package com.autografr.app.data.repository

import android.util.Log
import com.autografr.app.data.remote.datasource.FirebaseAuthDataSource
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.domain.model.BlockedUser
import com.autografr.app.domain.model.ReportReason
import com.autografr.app.domain.model.ReportTargetType
import com.autografr.app.domain.repository.SafetyRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SafetyRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource
) : SafetyRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBlockedUsers(): Flow<List<BlockedUser>> =
        authDataSource.observeAuthState()
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList())
                else firestoreDataSource.observeBlocked(uid).map { pairs ->
                    pairs.map { (id, name) -> BlockedUser(id, name) }.sortedBy { it.displayName.lowercase() }
                }
            }

    override fun observeBlockedIds(): Flow<Set<String>> =
        observeBlockedUsers().map { users -> users.mapTo(HashSet()) { it.userId } }

    override suspend fun blockUser(userId: String, displayName: String): Result<Unit> =
        write("Couldn't block this user. Check your connection and try again.") { me ->
            // Some screens only know the user's id (e.g. a reseller); keep a name for the block list.
            val name = displayName.ifBlank { firestoreDataSource.getUser(userId)?.displayName.orEmpty() }
            firestoreDataSource.blockUser(me, userId, name)
        }

    override suspend fun unblockUser(userId: String): Result<Unit> =
        write("Couldn't unblock this user. Check your connection and try again.") { me ->
            firestoreDataSource.unblockUser(me, userId)
        }

    override suspend fun report(
        targetType: ReportTargetType,
        targetId: String,
        targetUserId: String,
        reason: ReportReason,
        details: String
    ): Result<Unit> = write("Couldn't send your report. Check your connection and try again.") { me ->
        firestoreDataSource.createReport(me, targetType.name, targetId, targetUserId, reason.name, details.trim())
    }

    private suspend fun write(failure: String, block: suspend (String) -> Unit): Result<Unit> {
        val me = authDataSource.currentUserId ?: return Result.error("Sign in first.")
        return try {
            block(me)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, failure, e)
            Result.error(failure, e)
        }
    }

    companion object {
        private const val TAG = "SafetyRepository"
    }
}
