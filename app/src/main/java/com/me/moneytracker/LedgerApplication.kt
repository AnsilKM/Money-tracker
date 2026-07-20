package com.mee.moneytracker

import android.app.Application
import com.mee.moneytracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class LedgerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@LedgerApplication)
            modules(appModule)
        }
    }
}
