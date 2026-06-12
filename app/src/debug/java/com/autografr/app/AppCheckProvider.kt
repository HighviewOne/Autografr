package com.autografr.app

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

// Debug provider prints a token to logcat on first run; register it in
// Firebase Console → App Check → Apps → Manage debug tokens
fun appCheckProviderFactory(): AppCheckProviderFactory =
    DebugAppCheckProviderFactory.getInstance()
