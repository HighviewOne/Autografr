package com.autografr.app.domain.repository

import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    fun observeRequest(requestId: String): Flow<AutographRequest?>
    fun getCelebrityQueue(celebrityId: String): Flow<List<AutographRequest>>
    fun getFanRequests(fanId: String): Flow<List<AutographRequest>>
    suspend fun createRequest(request: AutographRequest): Result<AutographRequest>
    suspend fun acceptRequest(requestId: String): Result<Unit>
    suspend fun completeRequest(requestId: String, signedPhotoId: String): Result<Unit>
    suspend fun rejectRequest(requestId: String): Result<Unit>
}
