package com.autografr.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AutografrApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Provider differs per build variant: Play Integrity in release,
        // debug provider in debug (see src/debug and src/release source sets)
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(appCheckProviderFactory())
    }
}
