package com.yucox.pillpulse.domain.repository

import androidx.lifecycle.LiveData
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import org.mongodb.kbson.ObjectId

interface ILocaleAlarmRepository {
    fun fetchAlarms(): List<AlarmRealm>
    suspend fun changeAlarmStatus(alarm: ObjectId)
    suspend fun removeAlarm(alarmId: ObjectId)
    suspend fun createNewAlarm(alarm: AlarmRealm)
}