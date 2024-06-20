package com.yucox.pillpulse.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.model.IFirebasePillRepository
import com.yucox.pillpulse.model.PillTime
import kotlinx.coroutines.tasks.await
import org.mongodb.kbson.ObjectId
import java.lang.Exception
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebasePillRepository @Inject constructor(
    private val _database: FirebaseDatabase,
    private val _mainUserMail: String
) : IFirebasePillRepository {

    private fun saveUnsavedPills(
        dataKeyList: MutableList<String>,
        localConvertedPills: MutableList<PillTime>?
    ) {
        localConvertedPills?.forEach { pill ->
            if (!dataKeyList.contains(pill.key)) {
                println(pill.key)
                _database.getReference("Pills")
                    .child(pill.key)
                    .setValue(pill)
                println("kaydedildi")
            }
        }
    }

    suspend fun synchronizeData(
        localeIdList: List<String>,
        localConvertedPills: MutableList<PillTime>?
    ): List<PillTime>? {
        val tempPillList = ArrayList<PillTime>()
        val ref = _database.getReference("Pills")
        return suspendCoroutine { continuation ->

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dataKeyList = mutableListOf<String>()
                    if (!snapshot.exists()) {
                        continuation.resume(emptyList())
                        return
                    }

                    for (snap in snapshot.children) {
                        val userMail = snap.child("userMail").getValue()
                        if (userMail?.equals(_mainUserMail) == true) {
                            val id = snap.child("key").getValue()
                            dataKeyList.add(id.toString())
                            if (localeIdList.contains(id))
                                continue

                            val newKey = ObjectId().toHexString()
                            val pill = snap.getValue(PillTime::class.java)
                            ref.child(snap.key.toString()).child("key").setValue(newKey)
                            pill?.key = newKey
                            pill?.let {
                                tempPillList.add(it)
                            }
                        }
                    }
                    saveUnsavedPills(dataKeyList, localConvertedPills)
                    continuation.resume(tempPillList)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    override suspend fun fetchPillsInfo(
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

    override suspend fun deletePill(pillInfo: PillTime): Pair<Boolean, String?> {
        return try {
            _database.getReference("Pills")
                .child(pillInfo.key)
                .removeValue().await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    override suspend fun saveNewPill(pill: String, note: String): Boolean {
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

    override fun savePillWithSpecifiedTime(pill: PillTime): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        _database.getReference("Pills")
            .child(pill.key)
            .setValue(pill)
            .addOnCompleteListener {
                if (it.isSuccessful)
                    taskCompletionSource.setResult(true)
                else
                    taskCompletionSource.setResult(false)
            }
        return taskCompletionSource.task
    }
}