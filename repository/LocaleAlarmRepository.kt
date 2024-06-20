package com.yucox.pillpulse.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.yucox.pillpulse.model.AlarmRealm
import com.yucox.pillpulse.model.ILocaleAlarmRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class LocaleAlarmRepository @Inject constructor(
    private val realm: Realm
) : ILocaleAlarmRepository {

    override fun fetchAlarms(): LiveData<List<AlarmRealm>> {
        return realm.query<AlarmRealm>().asFlow().map { it.list }.asLiveData()
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