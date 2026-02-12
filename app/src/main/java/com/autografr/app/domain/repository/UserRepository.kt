package com.autografr.app.domain.repository

import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.model.User
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(userId: String): Flow<User?>
    fun getVerifiedCelebrities(): Flow<List<Celebrity>>
    suspend fun getUser(userId: String): Result<User>
    suspend fun getCelebrity(celebrityId: String): Result<Celebrity>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun updateProfileImage(userId: String, imageUri: String): Result<String>
}
