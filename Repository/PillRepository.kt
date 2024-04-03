package com.yucox.pillpulse.Repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.ViewModel.AlarmViewModel
import com.yucox.pillpulse.ViewModel.MainViewModel
import com.yucox.pillpulse.Model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PillRepository {
    private val _database = FirebaseDatabase.getInstance()
    private val _auth = FirebaseAuth.getInstance()
    private val _mainUserMail = _auth.currentUser?.email.toString()

    suspend fun fetchPillsInfo(
        viewModel: MainViewModel
    ): ArrayList<PillTime> {
        return suspendCoroutine { continuation ->
            val tempPillList = ArrayList<PillTime>()
            val ref = _database.getReference("Pills")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        tempPillList.add(
                            PillTime(
                                "Selam!",
                                "Buraya ilacını aldığın saati " +
                                        "ve notlarını kaydedebilirsin",
                                Date(),
                                _mainUserMail,
                                ""
                            )
                        )
                        return
                    }
                    for (snap in snapshot.children) {
                        val userMail = snap.child("userMail").getValue()
                        if (userMail?.equals(_mainUserMail) == true) {
                            tempPillList.add(snap.getValue(PillTime::class.java)!!)
                        }

                    }
                    continuation.resume(tempPillList)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    fun changePillNote(pillInfo: PillTime, note: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _database.getReference("Pills")
                .child(pillInfo.key)
                .child("note")
                .setValue(note)
        }
    }

    fun deleteBill(pillInfo: PillTime) {
        _database.getReference("Pills")
            .child(pillInfo.key)
            .removeValue()
    }

    fun saveNewPill(pill: String, note: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        val ref = _database.getReference("Pills").push()
        val key = ref.key.toString()
        var newPill = PillTime()
        if (note.isBlank()) {
            newPill = PillTime(
                pill,
                "Not almadınız.",
                Date(),
                _mainUserMail,
                key
            )
        } else {
            newPill = PillTime(
                pill,
                "Not almadınız.",
                Date(),
                _mainUserMail,
                key
            )
        }
        ref.setValue(newPill).addOnCompleteListener {
            if (it.isSuccessful) {
                taskCompletionSource.setResult(true)
            } else {
                taskCompletionSource.setResult(false)
            }
        }
        return taskCompletionSource.task
    }

    fun savePillWithSpecifiedTime(pill: PillTime, location: String): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        _database.getReference("Pills")
            .child(location)
            .setValue(pill)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    taskCompletionSource.setResult(true)
                else
                    taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }

    suspend fun savePillAlarm(key: String, viewModel: AlarmViewModel): Boolean {
        return suspendCoroutine { continuation ->
            val ref = _database.getReference("Alarms")
            ref.child(key).setValue(viewModel.alarm.value)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        continuation.resume(true)
                    } else {
                        continuation.resume(false)
                    }
                }
        }
    }
}