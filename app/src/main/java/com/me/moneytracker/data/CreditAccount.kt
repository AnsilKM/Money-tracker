package com.mee.moneytracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_accounts")
data class CreditAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "PERSON", "CREDIT_CARD", "LOAN"
    val limitAmount: Double? = null,
    val dueDateMillis: Long? = null,
    val repaymentType: String? = null, // "LUMP_SUM", "INSTALLMENTS"
    val installmentAmount: Double? = null,
    val interestRate: Double? = null,
    val isActive: Boolean = true,
    val tenureMonths: Int? = null,
    val dueDayOfMonth: Int? = null,
    val billDayOfMonth: Int? = null,
    val colorScheme: Int = 0,
    val emiStartMonth: Int? = null,
    val emiStartYear: Int? = null
)
