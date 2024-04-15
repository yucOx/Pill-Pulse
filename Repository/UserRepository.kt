package com.yucox.pillpulse.Repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.play.core.integrity.e
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.ViewModel.LoginViewModel
import com.yucox.pillpulse.Model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserRepository {
    private val _database = FirebaseDatabase.getInstance()
    private val _auth = FirebaseAuth.getInstance()
    private val _mainUserMail = _auth.currentUser?.email.toString()

    suspend fun fetchMainUserInfo(): UserInfo {
        return suspendCoroutine { continuation ->
            _database.getReference("UserInfo")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            return
                        }

                        for (snap in snapshot.children) {
                            val mail = snap.child("mail").getValue()
                            if (mail?.equals(_mainUserMail) == false)
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

    fun signOut() {
        _auth.signOut()
    }

    fun isAnyoneIn(): Int {
        return if (_auth.currentUser != null)
            1
        else 0
    }

    suspend fun logInCheck(mail: String?, pass: String?): Pair<Boolean, String?> {
        return try {
            _auth.signInWithEmailAndPassword(mail!!, pass!!).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    suspend fun createAccount(mail: String?, pass: String?): Pair<Boolean, String?> {
        return try {
            _auth.createUserWithEmailAndPassword(mail.toString(), pass.toString()).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    suspend fun saveUserInfo(user: UserInfo): Pair<Boolean, String?> {
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