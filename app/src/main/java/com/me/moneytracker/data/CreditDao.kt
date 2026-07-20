package com.mee.moneytracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {

    @Query("SELECT * FROM credit_accounts WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCreditAccounts(): Flow<List<CreditAccount>>

    @Query("SELECT * FROM credit_accounts WHERE id = :id")
    fun getCreditAccountById(id: Long): Flow<CreditAccount?>

    @Query("SELECT * FROM credit_transactions WHERE accountId = :accountId ORDER BY dateMillis DESC")
    fun getTransactionsForAccount(accountId: Long): Flow<List<CreditTransaction>>

    @Query("SELECT * FROM credit_transactions")
    fun getAllTransactions(): Flow<List<CreditTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditAccount(account: CreditAccount): Long

    @Update
    suspend fun updateCreditAccount(account: CreditAccount): Int

    @Delete
    suspend fun deleteCreditAccount(account: CreditAccount): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditTransaction(transaction: CreditTransaction): Long

    @Delete
    suspend fun deleteCreditTransaction(transaction: CreditTransaction): Int
}
