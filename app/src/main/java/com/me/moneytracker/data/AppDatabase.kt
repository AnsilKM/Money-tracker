package com.me.moneytracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Category::class, Expense::class, CreditAccount::class, CreditTransaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun creditDao(): CreditDao
}
