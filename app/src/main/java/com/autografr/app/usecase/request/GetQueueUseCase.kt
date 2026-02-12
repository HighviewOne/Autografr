package com.autografr.app.usecase.request

import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.repository.RequestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQueueUseCase @Inject constructor(
    private val requestRepository: RequestRepository
) {
    fun getCelebrityQueue(celebrityId: String): Flow<List<AutographRequest>> {
        return requestRepository.getCelebrityQueue(celebrityId)
    }

    fun getFanRequests(fanId: String): Flow<List<AutographRequest>> {
        return requestRepository.getFanRequests(fanId)
    }

    fun observeRequest(requestId: String): Flow<AutographRequest?> {
        return requestRepository.observeRequest(requestId)
    }
}
