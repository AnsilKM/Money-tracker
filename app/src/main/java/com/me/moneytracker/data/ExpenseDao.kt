package com.mee.moneytracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense): Int

    @Transaction
    @Query("""
        SELECT * FROM expenses 
        WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis 
        ORDER BY dateMillis DESC, id DESC
    """)
    fun getExpensesForDateRange(startMillis: Long, endMillis: Long): Flow<List<ExpenseWithCategory>>

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis
    """)
    fun getTotalSpendForDateRange(startMillis: Long, endMillis: Long): Flow<Double?>
}
