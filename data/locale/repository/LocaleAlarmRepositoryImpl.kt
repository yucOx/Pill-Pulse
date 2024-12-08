package com.yucox.pillpulse.data.locale.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.domain.repository.ILocaleAlarmRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class LocaleAlarmRepositoryImpl @Inject constructor(
    private val realm: Realm
) : ILocaleAlarmRepository {

    override fun fetchAlarms(): List<AlarmRealm> {
        val result = realm.query<AlarmRealm>().find()
        return result.toList()
    }

    override suspend fun changeAlarmStatus(alarmId: ObjectId) {
        realm.write {
            val queriedAlarm = query<AlarmRealm>(query = "id == $0", alarmId).first().find()
            queriedAlarm?.let {
                if (it.onOrOff == 0) {
                    it.onOrOff = 1
                } else {
                    it.onOrOff = 0
                }
            }
        }
    }

    override suspend fun removeAlarm(alarmId: ObjectId) {
        realm.write {
            val queriedAlarm = query<AlarmRealm>(query = "id == $0", alarmId).first().find()
            queriedAlarm?.let { delete(it) }
        }
    }

    override suspend fun createNewAlarm(alarm: AlarmRealm) {
        realm.write {
            copyToRealm(alarm)
        }
    }
}