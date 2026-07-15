package com.me.moneytracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_transactions",
    foreignKeys = [
        ForeignKey(
            entity = CreditAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"])]
)
data class CreditTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val amount: Double,
    val type: String, // "GIVEN", "RECEIVED"
    val dateMillis: Long,
    val note: String? = null,
    val isSettled: Boolean = false
)
