package com.imaniapp.uganda

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ImaniApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
} 