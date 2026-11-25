package com.autografr.app.data.remote.datasource

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val currentUserId: String? get() = auth.currentUser?.uid

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        return auth.signInWithEmailAndPassword(email, password).await().user
    }

    suspend fun createAccount(email: String, password: String): FirebaseUser? {
        return auth.createUserWithEmailAndPassword(email, password).await().user
    }

    fun signOut() {
        auth.signOut()
    }

    /** Firebase only lets a recently signed-in user delete their account, so confirm the password first. */
    suspend fun reauthenticate(password: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val email = user.email ?: throw IllegalStateException("Account has no email address")
        user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
    }

    suspend fun deleteCurrentUser() {
        auth.currentUser?.delete()?.await()
    }
}
