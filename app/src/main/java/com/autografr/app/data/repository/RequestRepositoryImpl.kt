package com.autografr.app.data.repository

import android.util.Log
import com.autografr.app.data.local.dao.RequestDao
import com.autografr.app.data.mapper.RequestMapper
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.model.RequestStatus
import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class RequestRepositoryImpl @Inject constructor(
    private val firestoreDataSource: FirestoreDataSource,
    private val requestDao: RequestDao
) : RequestRepository {

    override fun observeRequest(requestId: String): Flow<AutographRequest?> {
        return firestoreDataSource.getRequestById(requestId).map { dto ->
            dto?.let {
                requestDao.insertRequest(RequestMapper.dtoToEntity(it))
                RequestMapper.dtoToDomain(it)
            }
        }
    }

    override fun getCelebrityQueue(celebrityId: String): Flow<List<AutographRequest>> = channelFlow {
        launch {
            firestoreDataSource.getCelebrityQueue(celebrityId).collect { dtos ->
                requestDao.insertRequests(dtos.map { RequestMapper.dtoToEntity(it) })
            }
        }
        requestDao.getActiveQueue(celebrityId).collect { entities ->
            send(entities.map { RequestMapper.entityToDomain(it) })
        }
    }

    override fun getFanRequests(fanId: String): Flow<List<AutographRequest>> = channelFlow {
        launch {
            firestoreDataSource.getFanRequests(fanId).collect { dtos ->
                requestDao.insertRequests(dtos.map { RequestMapper.dtoToEntity(it) })
            }
        }
        requestDao.getRequestsByFan(fanId).collect { entities ->
            send(entities.map { RequestMapper.entityToDomain(it) })
        }
    }

    override suspend fun createRequest(request: AutographRequest): Result<AutographRequest> {
        return try {
            val newRequest = request.copy(
                id = UUID.randomUUID().toString(),
                status = RequestStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val dto = RequestMapper.domainToDto(newRequest)
            firestoreDataSource.saveRequest(dto)
            requestDao.insertRequest(RequestMapper.domainToEntity(newRequest))
            Result.success(newRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create request", e)
            Result.error(e.message ?: "Failed to create request", e)
        }
    }

    override suspend fun acceptRequest(requestId: String): Result<Unit> {
        return updateRequestStatus(requestId, RequestStatus.ACCEPTED)
    }

    override suspend fun completeRequest(requestId: String, signedPhotoId: String): Result<Unit> {
        return try {
            val dto = firestoreDataSource.getRequest(requestId)
                ?: return Result.error("Request not found")
            val updatedDto = dto.copy(
                status = RequestStatus.COMPLETED.name,
                signedPhotoId = signedPhotoId,
                updatedAt = System.currentTimeMillis()
            )
            firestoreDataSource.saveRequest(updatedDto)
            requestDao.insertRequest(RequestMapper.dtoToEntity(updatedDto))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete request $requestId", e)
            Result.error(e.message ?: "Failed to complete request", e)
        }
    }

    override suspend fun rejectRequest(requestId: String): Result<Unit> {
        return updateRequestStatus(requestId, RequestStatus.REJECTED)
    }

    private suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Result<Unit> {
        return try {
            val dto = firestoreDataSource.getRequest(requestId)
                ?: return Result.error("Request not found")
            val updatedDto = dto.copy(
                status = status.name,
                updatedAt = System.currentTimeMillis()
            )
            firestoreDataSource.saveRequest(updatedDto)
            requestDao.insertRequest(RequestMapper.dtoToEntity(updatedDto))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update request $requestId to $status", e)
            Result.error(e.message ?: "Failed to update request", e)
        }
    }

    companion object {
        private const val TAG = "RequestRepository"
    }
}
