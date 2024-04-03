package com.yucox.pillpulse.Repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.Model.AlarmInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AlarmRepository {
    private val _database = FirebaseDatabase.getInstance()
        .getReference("Alarms")
    private val _auth = FirebaseAuth.getInstance()
    private val _userMail = _auth.currentUser?.email.toString()

    suspend fun fetchAlarms(): ArrayList<AlarmInfo> {
        return suspendCoroutine { continuation ->
            val tempAlarmList = ArrayList<AlarmInfo>()
            _database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        return
                    }
                    for (snap in snapshot.children) {
                        if (snap.child("userMail").getValue()
                                ?.equals(_userMail) == false
                        ) {
                            continue
                        }
                        val alarm = snap.getValue(AlarmInfo::class.java)
                        alarm?.let {
                            tempAlarmList.add(it)
                        }
                    }
                    continuation.resume(tempAlarmList)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }


    fun saveAsOpen(
        alarmLocation: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val ref = _database.child(alarmLocation)
                .child("onOrOff")
            ref.setValue(1)
        }
    }

    fun saveAsClosed(
        alarmLocation: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val ref = _database.child(alarmLocation)
                .child("onOrOff")
            ref.setValue(0)
        }
    }

    fun saveRepeatingStatus(
        alarmLocation: String,
        repeating: Int
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            _database.child(alarmLocation)
                .child("repeating")
                .setValue(repeating)
        }

    }

    fun deleteAlarmFromDatabase(
        alarmLocation: String
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        _database.child(alarmLocation)
            .removeValue()
            .addOnSuccessListener {
                taskCompletionSource.setResult(true)
            }
            .addOnFailureListener {
                taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }
}