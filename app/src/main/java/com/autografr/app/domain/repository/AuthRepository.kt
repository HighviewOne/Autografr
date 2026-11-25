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

    /** Emails a password reset link. Succeeds even if no account uses [email], so it can't be used to probe for accounts. */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /**
     * Permanently deletes the signed-in account after confirming [password]: the profile,
     * profile image and sign-in. Photos and requests are cleaned up afterwards (see
     * account_deletions in firestore.rules).
     */
    suspend fun deleteAccount(password: String): Result<Unit>
}
