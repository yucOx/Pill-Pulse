package com.yucox.pillpulse.domain.manager

import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.domain.model.AlarmInfo

interface IAlarmManager {
    fun scheduleAlarm(alarm: AlarmInfo)
    fun scheduleAlarm(alarmRealm: AlarmRealm)
    fun cancelAlarm(alarm: AlarmRealm)
}