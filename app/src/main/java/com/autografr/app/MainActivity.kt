package com.autografr.app

import javax.inject.Inject
import com.autografr.app.usecase.auth.GetCurrentUserUseCase
import com.autografr.app.navigation.Screen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.autografr.app.navigation.AutografrNavHost
import com.autografr.app.ui.theme.AutografrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Firebase Auth keeps the session across launches; don't make users sign in again
        val startDestination =
            if (getCurrentUserUseCase.getCurrentUserId() != null) Screen.Feed else Screen.Login
        setContent {
            AutografrTheme {
                AutografrNavHost(startDestination = startDestination)
            }
        }
    }
}
