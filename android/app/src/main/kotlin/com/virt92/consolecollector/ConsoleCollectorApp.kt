package com.virt92.consolecollector

import android.app.Application
import com.virt92.consolecollector.di.AppContainer

class ConsoleCollectorApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
