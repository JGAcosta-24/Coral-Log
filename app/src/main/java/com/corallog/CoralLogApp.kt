package com.corallog

import android.app.Application
import com.corallog.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Custom Application class for Coral Log.
 * Initializes Koin dependency injection and sets up the offline-first environment.
 */
class CoralLogApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@CoralLogApp)
            modules(appModule)
        }
    }
}
