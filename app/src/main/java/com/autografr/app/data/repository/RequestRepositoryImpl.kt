package com.autografr.app.data.repository

import com.autografr.app.data.local.dao.RequestDao
import com.autografr.app.data.mapper.RequestMapper
import com.autografr.app.data.remote.datasource.FirestoreDataSource
import com.autografr.app.domain.model.AutographRequest
import com.autografr.app.domain.model.RequestStatus
import com.autografr.app.domain.repository.RequestRepository
import com.autografr.app.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    override fun getCelebrityQueue(celebrityId: String): Flow<List<AutographRequest>> {
        return firestoreDataSource.getCelebrityQueue(celebrityId).map { dtos ->
            val entities = dtos.map { RequestMapper.dtoToEntity(it) }
            requestDao.insertRequests(entities)
            dtos.map { RequestMapper.dtoToDomain(it) }
        }
    }

    override fun getFanRequests(fanId: String): Flow<List<AutographRequest>> {
        return firestoreDataSource.getFanRequests(fanId).map { dtos ->
            val entities = dtos.map { RequestMapper.dtoToEntity(it) }
            requestDao.insertRequests(entities)
            dtos.map { RequestMapper.dtoToDomain(it) }
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
            Result.error(e.message ?: "Failed to create request", e)
        }
    }

    override suspend fun acceptRequest(requestId: String): Result<Unit> {
        return updateRequestStatus(requestId, RequestStatus.ACCEPTED)
    }

    override suspend fun completeRequest(requestId: String, signedPhotoId: String): Result<Unit> {
        return try {
            val dto = firestoreDataSource.getRequestById(requestId).first()
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
            Result.error(e.message ?: "Failed to complete request", e)
        }
    }

    override suspend fun rejectRequest(requestId: String): Result<Unit> {
        return updateRequestStatus(requestId, RequestStatus.REJECTED)
    }

    private suspend fun updateRequestStatus(requestId: String, status: RequestStatus): Result<Unit> {
        return try {
            val dto = firestoreDataSource.getRequestById(requestId).first()
                ?: return Result.error("Request not found")
            val updatedDto = dto.copy(
                status = status.name,
                updatedAt = System.currentTimeMillis()
            )
            firestoreDataSource.saveRequest(updatedDto)
            requestDao.insertRequest(RequestMapper.dtoToEntity(updatedDto))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to update request", e)
        }
    }
}
