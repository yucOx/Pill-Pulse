package com.yucox.pillpulse.model

import androidx.lifecycle.LiveData
import org.mongodb.kbson.ObjectId

interface ILocaleAlarmRepository {
    fun fetchAlarms(): LiveData<List<AlarmRealm>>
    suspend fun changeAlarmStatus(alarm: ObjectId)
    suspend fun removeAlarm(alarmId: ObjectId)
    suspend fun createNewAlarm(alarm: AlarmRealm)
}