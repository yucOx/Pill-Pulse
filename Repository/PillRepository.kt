package com.yucox.pillpulse.Repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.Model.AlarmInfo
import com.yucox.pillpulse.Model.PillTime
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PillRepository {
    private val _database = FirebaseDatabase.getInstance()
    private val _auth = FirebaseAuth.getInstance()
    private val _mainUserMail = _auth.currentUser?.email.toString()

    suspend fun fetchPillsInfo(
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

    suspend fun deletePill(pillInfo: PillTime): Pair<Boolean, String?> {
        return try {
            _database.getReference("Pills")
                .child(pillInfo.key)
                .removeValue().await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    suspend fun saveNewPill(pill: String, note: String): Boolean {
        return suspendCoroutine { Continuation ->
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
            ref.setValue(newPill).addOnSuccessListener {
                Continuation.resume(true)
            }.addOnFailureListener {
                Continuation.resume(false)
            }
        }
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

    suspend fun savePillAlarm(key: String, alarm: AlarmInfo?): Pair<Boolean, String?> {
        return try {
            val ref = _database.getReference("Alarms")
            ref.child(key).setValue(alarm).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }
}