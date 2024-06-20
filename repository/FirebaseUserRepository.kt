package com.yucox.pillpulse.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.model.IFirebaseUserRepository
import com.yucox.pillpulse.model.UserInfo
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseUserRepository @Inject constructor(
    private val _database: FirebaseDatabase,
    private val _auth: FirebaseAuth
) : IFirebaseUserRepository {
    val mainUserMail = _auth.currentUser?.email.toString()

    override suspend fun fetchMainUserInfo(): UserInfo {
        return suspendCoroutine { continuation ->
            _database.getReference("UserInfo")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            return
                        }

                        for (snap in snapshot.children) {
                            val mail = snap.child("mail").getValue()
                            if (mail?.equals(mainUserMail) == false)
                                continue

                            val user = snap.getValue(UserInfo::class.java)
                            continuation.resume(user!!)
                            return
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

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

    override suspend fun saveUserInfo(user: UserInfo): Pair<Boolean, String?> {
        return try {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("UserInfo")
            ref.push().setValue(user).await()
            true to null

        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }
}