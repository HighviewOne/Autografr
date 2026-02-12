package com.autografr.app.data.repository

import com.autografr.app.data.local.dao.UserDao
import com.autografr.app.data.mapper.UserMapper
import com.autografr.app.data.remote.datasource.FirebaseAuthDataSource
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.data.remote.dto.UserDto
import com.autografr.app.domain.model.User
import com.autografr.app.domain.repository.AuthRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource,
    private val userDao: UserDao
) : AuthRepository {

    override val currentUserId: String? get() = authDataSource.currentUserId

    override val isLoggedIn: Boolean get() = authDataSource.currentUser != null

    override fun observeAuthState(): Flow<User?> {
        return authDataSource.observeAuthState().map { firebaseUser ->
            firebaseUser?.let {
                val dto = firestoreDataSource.getUser(it.uid)
                dto?.let { userDto -> UserMapper.dtoToDomain(userDto) }
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = authDataSource.signInWithEmail(email, password)
            val userDto = firestoreDataSource.getUser(firebaseUser.uid)
                ?: return Result.error("User profile not found")
            val user = UserMapper.dtoToDomain(userDto)
            userDao.insertUser(UserMapper.dtoToEntity(userDto))
            Result.success(user)
        } catch (e: Exception) {
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
            Result.error(e.message ?: "Registration failed", e)
        }
    }

    override fun logout() {
        authDataSource.signOut()
    }
}
