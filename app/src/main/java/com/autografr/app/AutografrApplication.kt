package com.autografr.app

import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AutografrApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.USE_FIREBASE_EMULATORS) connectFirebaseEmulators()
        // Provider differs per build variant: Play Integrity in release,
        // debug provider in debug (see src/debug and src/release source sets)
        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(appCheckProviderFactory())
    }

    /** Must run before any Firebase call. Ports match rules-tests/firebase.json. */
    private fun connectFirebaseEmulators() {
        val host = "127.0.0.1" // forwarded to this computer by `adb reverse`
        FirebaseAuth.getInstance().useEmulator(host, 9099)
        FirebaseFirestore.getInstance().useEmulator(host, 8181)
        FirebaseStorage.getInstance().useEmulator(host, 9199)
    }
}
