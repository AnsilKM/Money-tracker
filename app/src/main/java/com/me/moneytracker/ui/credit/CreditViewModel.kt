package com.mee.moneytracker.ui.credit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mee.moneytracker.data.Category
import com.mee.moneytracker.data.CategoryDao
import com.mee.moneytracker.data.CreditAccount
import com.mee.moneytracker.data.CreditDao
import com.mee.moneytracker.data.CreditTransaction
import com.mee.moneytracker.data.Expense
import com.mee.moneytracker.data.ExpenseDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import com.mee.moneytracker.reminder.ReminderManager

class CreditViewModel(
    private val creditDao: CreditDao,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) : ViewModel() {

    // List of active accounts
    val activeAccounts: StateFlow<List<CreditAccount>> = creditDao.getActiveCreditAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of default categories for moneytracker tracking
    val categories: StateFlow<List<Category>> = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive mapping of accountId -> balance
    val accountBalances: StateFlow<Map<Long, Double>> = creditDao.getAllTransactions()
        .map { txList ->
            txList.groupBy { it.accountId }.mapValues { (_, txs) ->
                txs.sumOf { tx ->
                    if (tx.type == "GIVEN") tx.amount else -tx.amount
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val accountTransactions: StateFlow<Map<Long, List<CreditTransaction>>> = creditDao.getAllTransactions()
        .map { txList ->
            txList.groupBy { it.accountId }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Selected account ID for detail view
    val selectedAccountId = MutableStateFlow<Long?>(null)

    // Reactive detail account flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedAccount: StateFlow<CreditAccount?> = selectedAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else creditDao.getCreditAccountById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive transactions list flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<CreditTransaction>> = selectedAccountId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else creditDao.getTransactionsForAccount(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectAccount(id: Long?) {
        selectedAccountId.value = id
    }

    fun addCreditAccount(
        name: String,
        type: String,
        limitAmount: Double?,
        dueDateMillis: Long?,
        repaymentType: String?,
        installmentAmount: Double?,
        interestRate: Double?,
        initialAmount: Double? = null,
        initialTxType: String? = null,
        tenureMonths: Int? = null,
        dueDayOfMonth: Int? = null,
        billDayOfMonth: Int? = null,
        colorScheme: Int = 0,
        emiStartMonth: Int? = null,
        emiStartYear: Int? = null,
        initialNote: String? = null
    ) {
        viewModelScope.launch {
            val account = CreditAccount(
                name = name,
                type = type,
                limitAmount = limitAmount,
                dueDateMillis = dueDateMillis,
                repaymentType = repaymentType,
                installmentAmount = installmentAmount,
                interestRate = interestRate,
                tenureMonths = tenureMonths,
                dueDayOfMonth = dueDayOfMonth,
                billDayOfMonth = billDayOfMonth,
                colorScheme = colorScheme,
                emiStartMonth = emiStartMonth,
                emiStartYear = emiStartYear
            )
            val accountId = creditDao.insertCreditAccount(account)
            
            // Schedule reminder based on account type
            if (type == "PERSON") {
                if (dueDateMillis != null) {
                    ReminderManager.scheduleReminder(
                        context = context,
                        accountId = accountId,
                        name = name,
                        type = type,
                        amount = initialAmount,
                        dueDay = null,
                        dueDateMillis = dueDateMillis,
                        isRecurring = false
                    )
                }
            } else if (dueDayOfMonth != null) {
                ReminderManager.scheduleReminder(
                    context = context,
                    accountId = accountId,
                    name = name,
                    type = type,
                    amount = installmentAmount,
                    dueDay = dueDayOfMonth,
                    dueDateMillis = null,
                    isRecurring = true
                )
            }

            if (initialAmount != null && initialAmount > 0 && initialTxType != null) {
                val transaction = CreditTransaction(
                    accountId = accountId,
                    amount = initialAmount,
                    type = initialTxType,
                    dateMillis = System.currentTimeMillis(),
                    note = initialNote ?: "Initial entry"
                )
                creditDao.insertCreditTransaction(transaction)
            }
        }
    }

    fun addCreditTransaction(
        accountId: Long,
        amount: Double,
        type: String, // "GIVEN", "RECEIVED"
        note: String?,
        dateMillis: Long,
        syncToLedger: Boolean = false,
        categoryId: Long? = null
    ) {
        viewModelScope.launch {
            // Save credit transaction
            val transaction = CreditTransaction(
                accountId = accountId,
                amount = amount,
                type = type,
                dateMillis = dateMillis,
                note = note
            )
            creditDao.insertCreditTransaction(transaction)

            // Sync with main moneytracker if requested
            if (syncToLedger && categoryId != null) {
                // If it is CREDIT_CARD charge (RECEIVED), it is an outflow / expense
                // If it is LOAN borrowing (RECEIVED), it is cash inflow (Income)
                // If it is LOAN repayment (GIVEN), it is cash outflow (Expense)
                // If it is PERSON lending (GIVEN), it is cash outflow (Expense)
                val isIncome = (type == "RECEIVED" && getAccountType(accountId) == "LOAN")
                
                val expense = Expense(
                    amount = amount,
                    categoryId = categoryId,
                    dateMillis = dateMillis,
                    note = note ?: "Credit Tx Sync",
                    isIncome = isIncome
                )
                expenseDao.insertExpense(expense)
            }
        }
    }

    fun deleteTransaction(transaction: CreditTransaction) {
        viewModelScope.launch {
            creditDao.deleteCreditTransaction(transaction)
        }
    }

    fun deleteAccount(account: CreditAccount) {
        viewModelScope.launch {
            ReminderManager.cancelReminder(context, account.id)
            creditDao.deleteCreditAccount(account)
        }
    }

    private suspend fun getAccountType(accountId: Long): String {
        return activeAccounts.value.firstOrNull { it.id == accountId }?.type ?: "PERSON"
    }
}
