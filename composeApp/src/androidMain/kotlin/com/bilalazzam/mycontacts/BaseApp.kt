package com.bilalazzam.mycontacts

import android.app.Application
import dev.icerock.moko.permissions.BuildConfig
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Napier.base(DebugAntilog())
        else Napier.takeLogarithm()
    }
}