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
import kotlinx.coroutines.tasks.await
import java.lang.Exception
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


    suspend fun saveAsOpen(
        alarmLocation: String
    ): Pair<Boolean, String?> {
        return try {
            val ref = _database.child(alarmLocation)
                .child("onOrOff")
            ref.setValue(1).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }

    }

    suspend fun saveAsClosed(
        alarmLocation: String
    ): Pair<Boolean, String?> {
        return try {
            val ref = _database.child(alarmLocation)
                .child("onOrOff")

            ref.setValue(0).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    suspend fun deleteAlarmFromDatabase(
        alarmLocation: String
    ): Pair<Boolean, String?> {
        return try {
            _database.child(alarmLocation)
                .removeValue().await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }
}