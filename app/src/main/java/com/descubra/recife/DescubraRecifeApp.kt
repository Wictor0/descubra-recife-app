package com.descubra.recife

import android.app.Application
import com.descubra.recife.di.AppContainer

class DescubraRecifeApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
