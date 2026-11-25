package com.autografr.app.data.remote.datasource

import com.autografr.app.data.remote.dto.RequestDto
import com.autografr.app.data.remote.dto.SignedPhotoDto
import com.autografr.app.data.remote.dto.TransactionDto
import com.autografr.app.data.remote.dto.UserDto
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import com.google.firebase.firestore.FieldValue
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
        const val ACCOUNT_DELETIONS_COLLECTION = "account_deletions"
        const val REPORTS_COLLECTION = "reports"
        const val BLOCKED_SUBCOLLECTION = "blocked"
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

    /**
     * Updates only the user-editable profile fields. A full set() would null out
     * verificationStatus and the celebrity/fan fields the client doesn't hold.
     */
    suspend fun updateUserProfile(
        userId: String,
        displayName: String,
        bio: String,
        profileImageUrl: String?,
        role: String
    ) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update(
                mapOf(
                    "displayName" to displayName,
                    "bio" to bio,
                    "profileImageUrl" to profileImageUrl,
                    "role" to role
                )
            )
            .await()
    }

    /** Records that [userId] deleted their account, so remaining data can be cleaned up by hand. */
    suspend fun markAccountDeletion(userId: String, role: String) {
        firestore.collection(ACCOUNT_DELETIONS_COLLECTION)
            .document(userId)
            .set(mapOf("uid" to userId, "role" to role, "requestedAt" to FieldValue.serverTimestamp()))
            .await()
    }

    suspend fun deleteUser(userId: String) {
        firestore.collection(USERS_COLLECTION).document(userId).delete().await()
    }

    fun observeUser(userId: String): Flow<UserDto?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
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
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(UserDto::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    // Blocking and reports

    /** Emits (blockedUserId, displayName) pairs from users/{userId}/blocked. */
    fun observeBlocked(userId: String): Flow<List<Pair<String, String>>> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(BLOCKED_SUBCOLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.map { it.id to (it.getString("displayName") ?: "") } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun blockUser(userId: String, blockedId: String, displayName: String) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(BLOCKED_SUBCOLLECTION)
            .document(blockedId)
            .set(mapOf("displayName" to displayName.take(100), "blockedAt" to FieldValue.serverTimestamp()))
            .await()
    }

    /** Deletes the user's whole block list (account deletion; Firestore keeps subcollections otherwise). */
    suspend fun deleteBlockedList(userId: String) {
        val docs = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(BLOCKED_SUBCOLLECTION)
            .get()
            .await()
        docs.documents.forEach { it.reference.delete().await() }
    }

    suspend fun unblockUser(userId: String, blockedId: String) {
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(BLOCKED_SUBCOLLECTION)
            .document(blockedId)
            .delete()
            .await()
    }

    suspend fun createReport(
        reporterId: String,
        targetType: String,
        targetId: String,
        targetUserId: String,
        reason: String,
        details: String
    ) {
        firestore.collection(REPORTS_COLLECTION)
            .document()
            .set(
                mapOf(
                    "reporterId" to reporterId,
                    "targetType" to targetType,
                    "targetId" to targetId,
                    "targetUserId" to targetUserId,
                    "reason" to reason,
                    "details" to details.take(500),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    // Signed Photos
    suspend fun savePhoto(photoDto: SignedPhotoDto) {
        firestore.collection(PHOTOS_COLLECTION)
            .document(photoDto.id)
            .set(photoDto)
            .await()
    }

    suspend fun getPhoto(photoId: String): SignedPhotoDto? {
        return firestore.collection(PHOTOS_COLLECTION)
            .document(photoId)
            .get()
            .await()
            .toObject(SignedPhotoDto::class.java)
    }

    fun getPhotoById(photoId: String): Flow<SignedPhotoDto?> = callbackFlow {
        val listener = firestore.collection(PHOTOS_COLLECTION)
            .document(photoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
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
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
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
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
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
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
                    return@addSnapshotListener
                }
                val photos = snapshot?.toObjects(SignedPhotoDto::class.java) ?: emptyList()
                trySend(photos)
            }
        awaitClose { listener.remove() }
    }

    fun getPhotosByOwner(ownerId: String): Flow<List<SignedPhotoDto>> = callbackFlow {
        val listener = firestore.collection(PHOTOS_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
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

    suspend fun getRequest(requestId: String): RequestDto? {
        return firestore.collection(REQUESTS_COLLECTION)
            .document(requestId)
            .get()
            .await()
            .toObject(RequestDto::class.java)
    }

    fun getRequestById(requestId: String): Flow<RequestDto?> = callbackFlow {
        val listener = firestore.collection(REQUESTS_COLLECTION)
            .document(requestId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
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
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
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
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
                    return@addSnapshotListener
                }
                val requests = snapshot?.toObjects(RequestDto::class.java) ?: emptyList()
                trySend(requests)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Atomically marks a listed photo SOLD to [buyerId] and records the transaction
     * built by [buildTransaction] from the photo as read inside the transaction.
     * Fails if the photo is gone, no longer listed, or already owned by the buyer.
     */
    suspend fun purchasePhoto(
        photoId: String,
        buyerId: String,
        buildTransaction: (SignedPhotoDto) -> TransactionDto
    ): Pair<SignedPhotoDto, TransactionDto> {
        val photoRef = firestore.collection(PHOTOS_COLLECTION).document(photoId)
        return firestore.runTransaction { tx ->
            val photo = tx.get(photoRef).toObject(SignedPhotoDto::class.java)
                ?: throw FirebaseFirestoreException("Listing not found", Code.NOT_FOUND)
            if (photo.status != "LISTED") {
                throw FirebaseFirestoreException("This item is no longer for sale", Code.FAILED_PRECONDITION)
            }
            if (photo.ownerId == buyerId) {
                throw FirebaseFirestoreException("You already own this item", Code.FAILED_PRECONDITION)
            }
            val transaction = buildTransaction(photo)
            tx.set(firestore.collection(TRANSACTIONS_COLLECTION).document(transaction.id), transaction)
            tx.update(photoRef, mapOf("status" to "SOLD", "ownerId" to buyerId))
            photo.copy(status = "SOLD", ownerId = buyerId) to transaction
        }.await()
    }

    // Transactions
    fun getTransactionsByUser(userId: String): Flow<List<TransactionDto>> = callbackFlow {
        val listener = firestore.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("buyerId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreDataSource", "Snapshot listener error", error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.toObjects(TransactionDto::class.java) ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }
}
