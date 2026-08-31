package com.pawedcat.app

import android.app.Application

class PawedCatApp : Application() {
    val serviceLocator: ServiceLocator by lazy {
        ServiceLocator.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
