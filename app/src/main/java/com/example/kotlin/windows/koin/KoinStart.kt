package com.example.kotlin.windows.koin

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KoinStart : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KoinStart)
            modules(moduleApp)
        }
    }
}