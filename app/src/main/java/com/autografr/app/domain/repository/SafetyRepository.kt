package com.autografr.app.domain.repository

import com.autografr.app.domain.model.BlockedUser
import com.autografr.app.domain.model.ReportReason
import com.autografr.app.domain.model.ReportTargetType
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow

/** Reporting and blocking (Play's user-generated content policy requires both). */
interface SafetyRepository {
    /** The signed-in user's block list; empty when signed out. */
    fun observeBlockedUsers(): Flow<List<BlockedUser>>
    fun observeBlockedIds(): Flow<Set<String>>
    suspend fun blockUser(userId: String, displayName: String): Result<Unit>
    suspend fun unblockUser(userId: String): Result<Unit>
    suspend fun report(
        targetType: ReportTargetType,
        targetId: String,
        targetUserId: String,
        reason: ReportReason,
        details: String
    ): Result<Unit>
}
