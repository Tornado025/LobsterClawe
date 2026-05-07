package com.lobsterclawe

import android.app.Application
import com.lobsterclawe.data.AppDatabase
import com.lobsterclawe.data.PrefsRepository
import com.lobsterclawe.network.OpenClawClient
import com.lobsterclawe.network.OpenRouterClient

class App : Application() {
    lateinit var prefs: PrefsRepository
    lateinit var database: AppDatabase
    lateinit var openRouterClient: OpenRouterClient
    lateinit var openClawClient: OpenClawClient

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsRepository(this)
        database = AppDatabase.build(this)
        openRouterClient = OpenRouterClient()
        openClawClient = OpenClawClient()
    }
}
