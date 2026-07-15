package com.me.moneytracker.ui.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.me.moneytracker.data.Category
import com.me.moneytracker.data.CategoryDao
import com.me.moneytracker.data.Expense
import com.me.moneytracker.data.ExpenseDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import java.util.Calendar
import java.util.Date

class AddExpenseViewModel(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) : ViewModel() {

    val expenseCategories: StateFlow<List<Category>> = categoryDao.getCategoriesByType(isIncome = false)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomeCategories: StateFlow<List<Category>> = categoryDao.getCategoriesByType(isIncome = true)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExpense(
        amount: Double,
        categoryId: Long,
        dateMillis: Long,
        note: String?,
        isIncome: Boolean,
        onSuccess: () -> Unit
    ) {
        Log.d("AddExpenseViewModel", "addExpense initiated: amount=$amount, categoryId=$categoryId, dateMillis=$dateMillis, isIncome=$isIncome, note=$note")
        viewModelScope.launch {
            if (amount > 0 && categoryId > 0) {
                try {
                    val insertedId = expenseDao.insertExpense(
                        Expense(
                            amount = amount,
                            categoryId = categoryId,
                            dateMillis = dateMillis,
                            note = note?.takeIf { it.isNotBlank() },
                            isIncome = isIncome
                        )
                    )
                    
                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val todayEnd = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    val isToday = dateMillis in todayStart..todayEnd

                    if (isToday) {
                        Log.i("AddExpenseViewModel", "Successfully added expense with ID $insertedId (for today)")
                    } else {
                        Log.w("AddExpenseViewModel", "Successfully added expense with ID $insertedId (for a past/future date: ${Date(dateMillis)}). Note: This entry will NOT show up on today's Home list, but will be visible in Reports.")
                    }

                    onSuccess()
                } catch (e: Exception) {
                    Log.e("AddExpenseViewModel", "Error database insertion failed for expense: amount=$amount, categoryId=$categoryId, dateMillis=$dateMillis", e)
                }
            } else {
                Log.e("AddExpenseViewModel", "Failed validation for adding expense: amount=$amount (must be > 0), categoryId=$categoryId (must be > 0)")
            }
        }
    }

    fun addCustomCategory(name: String, isIncome: Boolean, onCategoryAdded: (Category) -> Unit) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isNotEmpty()) {
                val existing = categoryDao.getCategoryByNameAndType(trimmedName, isIncome)
                if (existing != null) {
                    onCategoryAdded(existing)
                } else {
                    val newCat = Category(name = trimmedName, isDefault = false, isIncome = isIncome)
                    val newId = categoryDao.insertCategory(newCat)
                    onCategoryAdded(newCat.copy(id = newId))
                }
            }
        }
    }
}
