package com.autografr.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autografr.app.data.local.entity.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("SELECT * FROM autograph_requests WHERE id = :requestId")
    fun getRequestById(requestId: String): Flow<RequestEntity?>

    @Query("SELECT * FROM autograph_requests WHERE fanId = :fanId ORDER BY createdAt DESC")
    fun getRequestsByFan(fanId: String): Flow<List<RequestEntity>>

    @Query("SELECT * FROM autograph_requests WHERE celebrityId = :celebrityId ORDER BY createdAt DESC")
    fun getRequestsByCelebrity(celebrityId: String): Flow<List<RequestEntity>>

    @Query("SELECT * FROM autograph_requests WHERE celebrityId = :celebrityId AND status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS') ORDER BY createdAt ASC")
    fun getActiveQueue(celebrityId: String): Flow<List<RequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: RequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<RequestEntity>)

    @Query("DELETE FROM autograph_requests WHERE id = :requestId")
    suspend fun deleteRequest(requestId: String)
}
