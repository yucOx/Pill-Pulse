package com.yucox.pillpulse.data.remote.repository

import com.google.firebase.auth.FirebaseAuth
import com.yucox.pillpulse.domain.repository.IFirebaseUserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class FirebaseUserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
) : IFirebaseUserRepository {

    override suspend fun login(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun register(email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun isLoggedIn(): Boolean =
        auth.currentUser != null

    override fun getCurrentUserEmail(): String =
        auth.currentUser?.email ?: ""

    override fun signOut() {
        auth.signOut()
    }

    override fun getCurrentUserMail(): String =
        auth.currentUser?.email ?: ""
}