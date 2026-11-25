package com.autografr.app.data.repository

import android.util.Log
import com.autografr.app.data.local.dao.UserDao
import com.autografr.app.data.mapper.UserMapper
import com.autografr.app.data.remote.datasource.FirebaseAuthDataSource
import com.autografr.app.data.remote.datasource.FirebaseStorageDataSource
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.data.remote.dto.UserDto
import com.autografr.app.domain.model.User
import com.autografr.app.domain.repository.AuthRepository
import com.autografr.app.domain.util.Result
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUserId: String? get() = authDataSource.currentUserId

    override val isLoggedIn: Boolean get() = authDataSource.currentUser != null

    override fun observeAuthState(): Flow<User?> {
        return authDataSource.observeAuthState().map { firebaseUser ->
            firebaseUser?.let {
                try {
                    firestoreDataSource.getUser(it.uid)?.let { dto ->
                        userDao.insertUser(UserMapper.dtoToEntity(dto))
                        UserMapper.dtoToDomain(dto)
                    }
                } catch (e: Exception) {
                    // Offline (or token rejected): an uncaught throw here crashed the app at
                    // launch. Fall back to the profile cached from the last successful load.
                    Log.w(TAG, "Couldn't load profile for ${it.uid}; using cached copy", e)
                    userDao.getUserById(it.uid).first()?.let(UserMapper::entityToDomain)
                }
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signInWithEmail(email, password)
                ?: return Result.error("Sign in failed. Please try again.")
            val userDto = firestoreDataSource.getUser(firebaseUser.uid)
                ?: return Result.error("User profile not found")
            val user = UserMapper.dtoToDomain(userDto)
            userDao.insertUser(UserMapper.dtoToEntity(userDto))
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.error(e.message ?: "Login failed", e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val firebaseUser = authDataSource.createAccount(email, password)
                ?: return Result.error("Account creation failed. Please try again.")
            val userDto = UserDto(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = "FAN",
                createdAt = System.currentTimeMillis()
            )
            firestoreDataSource.saveUser(userDto)
            val user = UserMapper.dtoToDomain(userDto)
            userDao.insertUser(UserMapper.dtoToEntity(userDto))
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            Result.error(e.message ?: "Registration failed", e)
        }
    }

    override fun logout() {
        authDataSource.signOut()
    }

    override suspend fun deleteAccount(password: String): Result<Unit> {
        val userId = authDataSource.currentUserId ?: return Result.error("You're not signed in.")
        return try {
            // Re-authenticating first means the final Auth delete won't fail with
            // "requires recent login" after the profile is already gone.
            authDataSource.reauthenticate(password)
            val role = firestoreDataSource.getUser(userId)?.role ?: "FAN"
            firestoreDataSource.markAccountDeletion(userId, role)
            storageDataSource.deleteProfileImage(userId)
            firestoreDataSource.deleteUser(userId)
            authDataSource.deleteCurrentUser()
            userDao.deleteUser(userId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.error("That password isn't right.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Account deletion failed", e)
            Result.error("Couldn't delete your account. Check your connection and try again.", e)
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            authDataSource.sendPasswordReset(email)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseAuthInvalidUserException) {
            // Same answer whether or not the account exists.
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.error("That doesn't look like an email address.", e)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset failed", e)
            Result.error("Couldn't send the email. Check your connection and try again.", e)
        }
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
