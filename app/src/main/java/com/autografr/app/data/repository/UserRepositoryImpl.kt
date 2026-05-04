package com.autografr.app.data.repository

import android.net.Uri
import android.util.Log
import com.autografr.app.data.local.dao.UserDao
import com.autografr.app.data.mapper.UserMapper
import com.autografr.app.data.remote.datasource.FirebaseStorageDataSource
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.domain.model.Celebrity
import com.autografr.app.domain.model.User
import com.autografr.app.domain.repository.UserRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val storageDataSource: FirebaseStorageDataSource,
    private val userDao: UserDao
) : UserRepository {

    override fun observeUser(userId: String): Flow<User?> = channelFlow {
        launch {
            firestoreDataSource.observeUser(userId).collect { dto ->
                dto?.let { userDao.insertUser(UserMapper.dtoToEntity(it)) }
            }
        }
        userDao.getUserById(userId).collect { entity ->
            send(entity?.let { UserMapper.entityToDomain(it) })
        }
    }

    override fun getVerifiedCelebrities(): Flow<List<Celebrity>> = channelFlow {
        launch {
            firestoreDataSource.getVerifiedCelebrities().collect { dtos ->
                userDao.insertUsers(dtos.map { UserMapper.dtoToEntity(it) })
            }
        }
        userDao.getVerifiedCelebrities().collect { entities ->
            send(entities.map { UserMapper.entityToCelebrity(it) })
        }
    }

    override suspend fun getUser(userId: String): Result<User> {
        return try {
            val dto = firestoreDataSource.getUser(userId)
                ?: return Result.error("User not found")
            userDao.insertUser(UserMapper.dtoToEntity(dto))
            Result.success(UserMapper.dtoToDomain(dto))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user $userId", e)
            Result.error(e.message ?: "Failed to get user", e)
        }
    }

    override suspend fun getCelebrity(celebrityId: String): Result<Celebrity> {
        return try {
            val dto = firestoreDataSource.getUser(celebrityId)
                ?: return Result.error("Celebrity not found")
            Result.success(UserMapper.dtoToCelebrity(dto))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get celebrity $celebrityId", e)
            Result.error(e.message ?: "Failed to get celebrity", e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val dto = UserMapper.domainToDto(user)
            firestoreDataSource.saveUser(dto)
            userDao.insertUser(UserMapper.domainToEntity(user))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user ${user.id}", e)
            Result.error(e.message ?: "Failed to update user", e)
        }
    }

    override suspend fun updateProfileImage(userId: String, imageUri: String): Result<String> {
        return try {
            val downloadUrl = storageDataSource.uploadProfileImage(userId, Uri.parse(imageUri))
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload profile image for $userId", e)
            Result.error(e.message ?: "Failed to upload profile image", e)
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}
