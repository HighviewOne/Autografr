package com.autografr.app.data.remote.datasource

import com.autografr.app.data.remote.dto.RequestDto
import com.autografr.app.data.remote.dto.SignedPhotoDto
import com.autografr.app.data.remote.dto.TransactionDto
import com.autografr.app.data.remote.dto.UserDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        const val USERS_COLLECTION = "users"
        const val PHOTOS_COLLECTION = "signed_photos"
        const val REQUESTS_COLLECTION = "autograph_requests"
        const val TRANSACTIONS_COLLECTION = "transactions"
    }

    // Users
    suspend fun getUser(userId: String): UserDto? {
        return firestore.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
            .toObject(UserDto::class.java)
    }

    suspend fun saveUser(userDto: UserDto) {
        firestore.collection(USERS_COLLECTION)
            .document(userDto.id)
            .set(userDto)
            .await()
    }

    fun observeUser(userId: String): Flow<UserDto?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(UserDto::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun getVerifiedCelebrities(): Flow<List<UserDto>> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .whereEqualTo("role", "CELEBRITY")
            .whereEqualTo("verificationStatus", "VERIFIED")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(UserDto::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    // Signed Photos
    suspend fun savePhoto(photoDto: SignedPhotoDto) {
        firestore.collection(PHOTOS_COLLECTION)
            .document(photoDto.id)
            .set(photoDto)
            .await()
    }

    fun getPhotoById(photoId: String): Flow<SignedPhotoDto?> = callbackFlow {
        val listener = firestore.collection(PHOTOS_COLLECTION)
            .document(photoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(SignedPhotoDto::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun getListedPhotos(): Flow<List<SignedPhotoDto>> = callbackFlow {
        val listener = firestore.collection(PHOTOS_COLLECTION)
            .whereEqualTo("status", "LISTED")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val photos = snapshot?.toObjects(SignedPhotoDto::class.java) ?: emptyList()
                trySend(photos)
            }
        awaitClose { listener.remove() }
    }

    fun getTrendingPhotos(limit: Int): Flow<List<SignedPhotoDto>> = callbackFlow {
        val listener = firestore.collection(PHOTOS_COLLECTION)
            .whereEqualTo("status", "LISTED")
            .orderBy("likeCount", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val photos = snapshot?.toObjects(SignedPhotoDto::class.java) ?: emptyList()
                trySend(photos)
            }
        awaitClose { listener.remove() }
    }

    fun getPhotosByCelebrity(celebrityId: String): Flow<List<SignedPhotoDto>> = callbackFlow {
        val listener = firestore.collection(PHOTOS_COLLECTION)
            .whereEqualTo("celebrityId", celebrityId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val photos = snapshot?.toObjects(SignedPhotoDto::class.java) ?: emptyList()
                trySend(photos)
            }
        awaitClose { listener.remove() }
    }

    // Requests
    suspend fun saveRequest(requestDto: RequestDto) {
        firestore.collection(REQUESTS_COLLECTION)
            .document(requestDto.id)
            .set(requestDto)
            .await()
    }

    fun getRequestById(requestId: String): Flow<RequestDto?> = callbackFlow {
        val listener = firestore.collection(REQUESTS_COLLECTION)
            .document(requestId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(RequestDto::class.java))
            }
        awaitClose { listener.remove() }
    }

    fun getCelebrityQueue(celebrityId: String): Flow<List<RequestDto>> = callbackFlow {
        val listener = firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("celebrityId", celebrityId)
            .whereIn("status", listOf("PENDING", "ACCEPTED", "IN_PROGRESS"))
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(RequestDto::class.java) ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    fun getFanRequests(fanId: String): Flow<List<RequestDto>> = callbackFlow {
        val listener = firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("fanId", fanId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(RequestDto::class.java) ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    // Transactions
    suspend fun saveTransaction(transactionDto: TransactionDto) {
        firestore.collection(TRANSACTIONS_COLLECTION)
            .document(transactionDto.id)
            .set(transactionDto)
            .await()
    }

    fun getTransactionsByUser(userId: String): Flow<List<TransactionDto>> = callbackFlow {
        val listener = firestore.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("buyerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(TransactionDto::class.java) ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }
}
