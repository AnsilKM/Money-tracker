package com.mee.moneytracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mee.moneytracker.data.Expense
import com.mee.moneytracker.data.ExpenseDao
import com.mee.moneytracker.data.ExpenseWithCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import android.util.Log

class HomeViewModel(private val expenseDao: ExpenseDao) : ViewModel() {

    val currentDate = MutableStateFlow(Calendar.getInstance())

    private fun getStartOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayExpenses: StateFlow<List<ExpenseWithCategory>> = currentDate
        .flatMapLatest { cal ->
            expenseDao.getExpensesForDateRange(getStartOfDay(cal), getEndOfDay(cal))
        }
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

    fun moveToPreviousDay() {
        val cal = currentDate.value.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -1)
        currentDate.value = cal
    }

    private fun isFutureDay(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        if (cal.get(Calendar.YEAR) > today.get(Calendar.YEAR)) return true
        if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            if (cal.get(Calendar.DAY_OF_YEAR) > today.get(Calendar.DAY_OF_YEAR)) return true
        }
        return false
    }

    fun moveToNextDay() {
        val cal = currentDate.value.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, 1)
        if (!isFutureDay(cal)) {
            currentDate.value = cal
        }
    }

    fun selectDate(year: Int, month: Int, day: Int) {
        val cal = currentDate.value.clone() as Calendar
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, day)
        if (!isFutureDay(cal)) {
            currentDate.value = cal
        }
    }

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
