package com.me.moneytracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.me.moneytracker.data.Expense
import com.me.moneytracker.data.ExpenseDao
import com.me.moneytracker.data.ExpenseWithCategory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import android.util.Log

class HomeViewModel(private val expenseDao: ExpenseDao) : ViewModel() {

    fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    val todayExpenses: StateFlow<List<ExpenseWithCategory>> = expenseDao
        .getExpensesForDateRange(getStartOfToday(), getEndOfToday())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayTotalIncome: StateFlow<Double> = todayExpenses
        .map { list -> list.filter { it.expense.isIncome }.sumOf { it.expense.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val todayTotalExpense: StateFlow<Double> = todayExpenses
        .map { list -> list.filter { !it.expense.isIncome }.sumOf { it.expense.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val todayBalance: StateFlow<Double> = todayExpenses
        .map { list -> list.sumOf { if (it.expense.isIncome) it.expense.amount else -it.expense.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun deleteExpense(expense: Expense) {
        Log.d("HomeViewModel", "deleteExpense initiated: id=${expense.id}, amount=${expense.amount}, note=${expense.note}")
        viewModelScope.launch {
            try {
                val deletedCount = expenseDao.deleteExpense(expense)
                if (deletedCount > 0) {
                    Log.i("HomeViewModel", "Successfully deleted expense with ID: ${expense.id}")
                } else {
                    Log.w("HomeViewModel", "No rows deleted when attempting to delete expense with ID: ${expense.id}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error: failed to delete expense with ID: ${expense.id}", e)
            }
        }
    }
}
