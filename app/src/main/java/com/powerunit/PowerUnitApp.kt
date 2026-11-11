package com.powerunit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PowerUnitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
        } catch (_: UnsatisfiedLinkError) {
            // Handled or already loaded
        }
    }
}
