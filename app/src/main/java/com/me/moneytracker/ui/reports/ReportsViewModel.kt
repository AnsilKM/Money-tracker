package com.mee.moneytracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mee.moneytracker.data.ExpenseDao
import com.mee.moneytracker.data.ExpenseWithCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log
import java.util.Date

data class CategoryBreakdown(
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class DailyTotal(
    val dayName: String,
    val dateLabel: String,
    val amount: Double,
    val calendar: Calendar
)

data class WeeklyTotal(
    val weekLabel: String,
    val amount: Double,
    val calendar: Calendar
)

class ReportsViewModel(private val expenseDao: ExpenseDao) : ViewModel() {

    val tabIndex = MutableStateFlow(0) // 0: Daily, 1: Weekly, 2: Monthly
    val currentDate = MutableStateFlow(Calendar.getInstance())
    val showIncome = MutableStateFlow(false) // false = Expenses, true = Income

    // Flow that emits start and end millis for the selected period range
    @OptIn(ExperimentalCoroutinesApi::class)
    private val periodRange = combine(tabIndex, currentDate) { tab, date ->
        calculatePeriodRange(tab, date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = calculatePeriodRange(0, Calendar.getInstance())
    )

    private fun calculatePeriodRange(tab: Int, calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        val range = when (tab) {
            0 -> { // Daily
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            1 -> { // Weekly (starts on Monday, ends on Sunday)
                val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysToSubtract = (currentDayOfWeek - Calendar.MONDAY + 7) % 7
                cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                
                cal.add(Calendar.DAY_OF_YEAR, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            else -> { // Monthly
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
        }
        Log.d("ReportsViewModel", "calculatePeriodRange: tab=$tab, start=${Date(range.first)}, end=${Date(range.second)}")
        return range
    }

    // Period display label
    val dateLabel: StateFlow<String> = combine(tabIndex, currentDate) { tab, date ->
        val cal = date.clone() as Calendar
        when (tab) {
            0 -> {
                SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(cal.time)
            }
            1 -> {
                val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysToSubtract = (currentDayOfWeek - Calendar.MONDAY + 7) % 7
                cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
                val startStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, 6)
                val endStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
                "$startStr - $endStr"
            }
            else -> {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    // Query database reactively whenever the period range changes
    @OptIn(ExperimentalCoroutinesApi::class)
    private val allEntries: StateFlow<List<ExpenseWithCategory>> = periodRange
        .flatMapLatest { range ->
            Log.d("ReportsViewModel", "Querying database for date range: startMillis=${range.first} (${Date(range.first)}), endMillis=${range.second} (${Date(range.second)})")
            expenseDao.getExpensesForDateRange(range.first, range.second)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenses: StateFlow<List<ExpenseWithCategory>> = combine(allEntries, showIncome) { list, income ->
        val filtered = list.filter { it.expense.isIncome == income }
        Log.i("ReportsViewModel", "Generated report entries list: size=${filtered.size}, showIncomeMode=$income")
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalSpend: StateFlow<Double> = expenses.map { list ->
        list.sumOf { it.expense.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val categoryBreakdown: StateFlow<List<CategoryBreakdown>> = expenses.map { list ->
        val total = list.sumOf { it.expense.amount }
        if (total == 0.0) {
            Log.d("ReportsViewModel", "Category breakdown calculated: total=0.0 (empty)")
            return@map emptyList()
        }
        
        val breakdown = list.groupBy { it.category.name }
            .map { (catName, items) ->
                val amount = items.sumOf { it.expense.amount }
                val percentage = ((amount / total) * 100).toFloat()
                CategoryBreakdown(catName, amount, percentage)
            }
            .sortedByDescending { it.amount }
        Log.d("ReportsViewModel", "Category breakdown calculated: categoriesCount=${breakdown.size}, totalAmount=$total")
        breakdown
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun isPeriodInFuture(tab: Int, calendar: Calendar): Boolean {
        val cal = calendar.clone() as Calendar
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return when (tab) {
            0 -> { // Daily: start of that day is after today
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis > startOfToday
            }
            1 -> { // Weekly: start of that week is after today
                val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val daysToSubtract = (currentDayOfWeek - Calendar.MONDAY + 7) % 7
                cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis > startOfToday
            }
            else -> { // Monthly: start of that month is after today
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis > startOfToday
            }
        }
    }

    val isNextPeriodEnabled: StateFlow<Boolean> = combine(tabIndex, currentDate) { tab, date ->
        val cal = date.clone() as Calendar
        when (tab) {
            0 -> cal.add(Calendar.DAY_OF_YEAR, 1)
            1 -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            2 -> cal.add(Calendar.MONTH, 1)
        }
        !isPeriodInFuture(tab, cal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun navigatePeriod(direction: Int) { // -1: Previous, 1: Next
        val cal = currentDate.value.clone() as Calendar
        when (tabIndex.value) {
            0 -> cal.add(Calendar.DAY_OF_YEAR, direction)
            1 -> cal.add(Calendar.WEEK_OF_YEAR, direction)
            2 -> cal.add(Calendar.MONTH, direction)
        }
        if (direction <= 0 || !isPeriodInFuture(tabIndex.value, cal)) {
            currentDate.value = cal
        }
    }

    fun selectDate(year: Int, month: Int, dayOfMonth: Int) {
        val cal = currentDate.value.clone() as Calendar
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        if (!isPeriodInFuture(tabIndex.value, cal)) {
            currentDate.value = cal
        }
    }

    fun setTab(index: Int) {
        tabIndex.value = index
        currentDate.value = Calendar.getInstance()
    }

    fun toggleIncomeMode(income: Boolean) {
        showIncome.value = income
    }

    val weeklyDailyTotals: StateFlow<List<DailyTotal>> = combine(expenses, periodRange) { list, range ->
        val firstDayCal = Calendar.getInstance().apply { timeInMillis = range.first }
        val totals = mutableListOf<DailyTotal>()
        
        for (i in 0..6) {
            val dayCal = firstDayCal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_WEEK, i)
            
            val dayStart = dayCal.clone() as Calendar
            dayStart.set(Calendar.HOUR_OF_DAY, 0)
            dayStart.set(Calendar.MINUTE, 0)
            dayStart.set(Calendar.SECOND, 0)
            dayStart.set(Calendar.MILLISECOND, 0)
            val dayStartMillis = dayStart.timeInMillis
            
            val dayEnd = dayCal.clone() as Calendar
            dayEnd.set(Calendar.HOUR_OF_DAY, 23)
            dayEnd.set(Calendar.MINUTE, 59)
            dayEnd.set(Calendar.SECOND, 59)
            dayEnd.set(Calendar.MILLISECOND, 999)
            val dayEndMillis = dayEnd.timeInMillis
            
            val dayAmount = list.filter { it.expense.dateMillis in dayStartMillis..dayEndMillis }
                                .sumOf { it.expense.amount }
            
            val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(dayCal.time)
            val dateLabel = SimpleDateFormat("dd MMM", Locale.getDefault()).format(dayCal.time)
            totals.add(DailyTotal(dayName, dateLabel, dayAmount, dayCal))
        }
        totals
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val monthlyWeeklyTotals: StateFlow<List<WeeklyTotal>> = combine(expenses, periodRange) { list, range ->
        val monthStartCal = Calendar.getInstance().apply { 
            timeInMillis = range.first
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val monthEndCal = Calendar.getInstance().apply { 
            timeInMillis = range.second
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        
        val totals = mutableListOf<WeeklyTotal>()
        
        val cal = monthStartCal.clone() as Calendar
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = (dayOfWeek - Calendar.MONDAY + 7) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        
        var weekIndex = 1
        while (cal.timeInMillis <= monthEndCal.timeInMillis) {
            val weekStartCal = cal.clone() as Calendar
            weekStartCal.set(Calendar.HOUR_OF_DAY, 0)
            weekStartCal.set(Calendar.MINUTE, 0)
            weekStartCal.set(Calendar.SECOND, 0)
            weekStartCal.set(Calendar.MILLISECOND, 0)
            
            val weekEndCal = cal.clone() as Calendar
            weekEndCal.add(Calendar.DAY_OF_YEAR, 6)
            weekEndCal.set(Calendar.HOUR_OF_DAY, 23)
            weekEndCal.set(Calendar.MINUTE, 59)
            weekEndCal.set(Calendar.SECOND, 59)
            weekEndCal.set(Calendar.MILLISECOND, 999)
            
            val wStartMillis = maxOf(weekStartCal.timeInMillis, monthStartCal.timeInMillis)
            val wEndMillis = minOf(weekEndCal.timeInMillis, monthEndCal.timeInMillis)
            
            val weekAmount = list.filter { it.expense.dateMillis in wStartMillis..wEndMillis }
                                 .sumOf { it.expense.amount }
            
            val startStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(weekStartCal.time)
            val endStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(weekEndCal.time)
            val label = "Week $weekIndex: $startStr - $endStr"
            
            totals.add(WeeklyTotal(label, weekAmount, weekStartCal))
            
            cal.add(Calendar.DAY_OF_YEAR, 7)
            weekIndex++
        }
        totals
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectDayAndNavigate(calendar: Calendar) {
        currentDate.value = calendar
        tabIndex.value = 0 // Navigate to Daily
    }

    fun selectWeekAndNavigate(calendar: Calendar) {
        currentDate.value = calendar
        tabIndex.value = 1 // Navigate to Weekly
    }
}
