package com.devhyeon.phoneauth

import android.app.Application
import com.devhyeon.phoneauth.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

open class DevApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DevApp)
            modules(
                listOf(
                    AppModule
                )
            )
        }
    }
}