package com.sellinout

import android.app.Application
import com.pixplicity.easyprefs.library.Prefs
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SellApp : Application() {

    companion object {
        lateinit var INSTANCE: SellApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        Prefs.Builder()
            .setContext(this)
            .setPrefsName("Sell")
            .build()
    }
}