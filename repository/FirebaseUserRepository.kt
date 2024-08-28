package com.yucox.pillpulse.repository

import com.google.firebase.auth.FirebaseAuth
import com.yucox.pillpulse.model.IFirebaseUserRepository
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val _auth: FirebaseAuth
) : IFirebaseUserRepository {

    override fun signOut() {
        _auth.signOut()
    }

    override fun isAnyoneIn(): Int {
        return if (_auth.currentUser != null)
            1
        else 0
    }

    override suspend fun logInCheck(mail: String?, pass: String?): Pair<Boolean, String?> {
        return try {
            _auth.signInWithEmailAndPassword(mail!!, pass!!).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    override suspend fun createAccount(mail: String?, pass: String?): Pair<Boolean, String?> {
        return try {
            _auth.createUserWithEmailAndPassword(mail.toString(), pass.toString()).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

}