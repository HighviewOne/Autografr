package com.autografr.app.usecase.request

import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.repository.SafetyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetQueueUseCase @Inject constructor(
    private val requestRepository: RequestRepository,
    private val safetyRepository: SafetyRepository
) {
    fun getCelebrityQueue(celebrityId: String): Flow<List<AutographRequest>> {
        // Blocked fans can't send new requests (firestore.rules); hide ones they sent before.
        return combine(requestRepository.getCelebrityQueue(celebrityId), safetyRepository.observeBlockedIds()) { queue, blocked ->
            queue.filterNot { it.fanId in blocked }
        }
    }

    fun getFanRequests(fanId: String): Flow<List<AutographRequest>> {
        return requestRepository.getFanRequests(fanId)
    }

    fun observeRequest(requestId: String): Flow<AutographRequest?> {
        return requestRepository.observeRequest(requestId)
    }
}
