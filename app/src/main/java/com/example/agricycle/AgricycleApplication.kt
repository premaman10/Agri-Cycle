package com.example.agricycle

import android.app.Application
import com.google.firebase.FirebaseApp

class AgricycleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 