package com.me.moneytracker.di

import androidx.room.Room
import com.me.moneytracker.data.AppDatabase
import com.me.moneytracker.ui.addexpense.AddExpenseViewModel
import com.me.moneytracker.ui.home.HomeViewModel
import com.me.moneytracker.ui.reports.ReportsViewModel
import com.me.moneytracker.ui.credit.CreditViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Room database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "moneytracker.db"
        ).fallbackToDestructiveMigration().build()
    }

    // DAOs
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().expenseDao() }
    single { get<AppDatabase>().creditDao() }

    // ViewModels
    viewModelOf(::HomeViewModel)
    viewModelOf(::AddExpenseViewModel)
    viewModelOf(::ReportsViewModel)
    viewModelOf(::CreditViewModel)
}
