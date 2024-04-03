package com.yucox.pillpulse.Repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.ViewModel.LoginViewModel
import com.yucox.pillpulse.ViewModel.MainViewModel
import com.yucox.pillpulse.Model.UserInfo
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserRepository {
    private val _database = FirebaseDatabase.getInstance()
    private val _auth = FirebaseAuth.getInstance()
    private val _mainUserMail = _auth.currentUser?.email.toString()

    suspend fun fetchMainUserInfo(viewModel: MainViewModel): UserInfo {
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

    fun takeMe(viewModel: LoginViewModel) {
        val mail = viewModel.user.value?.mail
        val pass = viewModel.pass.value
        _auth.signInWithEmailAndPassword(mail!!, pass!!)
            .addOnSuccessListener {
                viewModel.updateStatus(1)
            }
            .addOnFailureListener {
                viewModel.updateErrorMessage(it.message.toString())
            }
    }

    fun createAccount(viewModel: LoginViewModel) {
        val mail = viewModel.user.value?.mail.toString()
        val pass = viewModel.pass.value.toString()
        _auth.createUserWithEmailAndPassword(mail, pass)
            .addOnSuccessListener {
                viewModel.updateStatus(1)
            }
            .addOnFailureListener {
                viewModel.updateErrorMessage(it.message.toString())
            }
    }

    fun saveUserInfo(viewModel: LoginViewModel): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("UserInfo")
        ref.setValue(viewModel.user).addOnCompleteListener {
            if (it.isSuccessful)
                taskCompletionSource.setResult(true)
            else
                taskCompletionSource.setResult(false)
        }
        return taskCompletionSource.task
    }
}