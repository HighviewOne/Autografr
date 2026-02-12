package com.autografr.app.domain.repository

import com.autografr.app.domain.model.User
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val isLoggedIn: Boolean
    fun observeAuthState(): Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    fun logout()
}
