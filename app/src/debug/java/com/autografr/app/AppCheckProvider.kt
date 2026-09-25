package com.autografr.app

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

// Debug provider prints a token to logcat on first run; register it in
// Firebase Console → App Check → Apps → Manage debug tokens.
// Every install gets its own unregistered token, so a shared app-debug.apk
// is locked out once enforcement is on — see README "App Check".
fun appCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()
