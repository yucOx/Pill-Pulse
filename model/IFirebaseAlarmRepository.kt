package com.yucox.pillpulse.model

interface IFirebaseAlarmRepository {
    suspend fun synchronizeData(
        localAlarmList: MutableList<String>,
        tempData: MutableList<AlarmInfo>
    ): ArrayList<AlarmInfo>

    suspend fun saveAsOpen(alarmLocation: String): Pair<Boolean, String>
    suspend fun saveAsClosed(alarmLocation: String): Pair<Boolean, String?>
    suspend fun deleteAlarmFromDatabase(alarmLocation: String): Pair<Boolean, String?>

}