package com.yucox.pillpulse.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.model.IFirebaseAlarmRepository
import kotlinx.coroutines.tasks.await
import org.mongodb.kbson.ObjectId
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FirebaseAlarmRepository @Inject constructor(
    private val _database: FirebaseDatabase,
    private val _auth: FirebaseAuth
) : IFirebaseAlarmRepository {

    val alarmRef = _database.getReference("Alarms")

    private fun saveUnsavedAlarms(
        dataIdList: MutableList<String>,
        localAlarms: MutableList<AlarmInfo>,
    ) {
        localAlarms.forEach { localAlarm ->
            if (!dataIdList.contains(localAlarm.alarmLocation)
                || dataIdList.isEmpty()
            ) {
                alarmRef.push().setValue(localAlarm)
            }
        }
    }

    override suspend fun synchronizeData(
        localeIdList: MutableList<String>,
        localAlarms: MutableList<AlarmInfo>
    ): ArrayList<AlarmInfo> {
        val userMail = _auth.currentUser?.email.toString()
        val dataIdList = mutableListOf<String>()
        return suspendCoroutine { continuation ->
            val tempAlarmList = ArrayList<AlarmInfo>()
            alarmRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        saveUnsavedAlarms(dataIdList, localAlarms)
                        return
                    }
                    for (snap in snapshot.children) {
                        val dataMail = snap.child("userMail").getValue()
                        if (dataMail?.equals(userMail) == false) {
                            continue
                        }

                        val dataId = snap.child("alarmLocation").getValue().toString()
                        dataIdList.add(dataId)
                        if (localeIdList.contains(dataId)) {
                            continue
                        }

                        val alarm = snap.getValue(AlarmInfo::class.java)
                        alarm?.let { safeAlarm ->
                            val newId = ObjectId().toHexString()
                            safeAlarm.alarmLocation = newId
                            tempAlarmList.add(safeAlarm)
                            localeIdList.add(newId)
                            alarmRef.child(snap.key.toString())
                                .child("alarmLocation")
                                .setValue(newId)
                        }
                    }
                    saveUnsavedAlarms(dataIdList, localAlarms)
                    continuation.resume(tempAlarmList)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    override suspend fun saveAsOpen(
        alarmLocation: String
    ): Pair<Boolean, String> {
        return try {
            val ref = alarmRef.child(alarmLocation)
                .child("onOrOff")
            ref.setValue(1).await()
            true to ""
        } catch (e: Exception) {
            false to e.localizedMessage
        }

    }

    override suspend fun saveAsClosed(
        alarmLocation: String
    ): Pair<Boolean, String?> {
        return try {
            val ref = alarmRef.child(alarmLocation)
                .child("onOrOff")

            ref.setValue(0).await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }

    override suspend fun deleteAlarmFromDatabase(
        alarmLocation: String
    ): Pair<Boolean, String?> {
        return try {
            alarmRef.child(alarmLocation)
                .removeValue().await()
            true to null
        } catch (e: Exception) {
            false to e.localizedMessage
        }
    }
}