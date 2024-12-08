package com.yucox.pillpulse.presentation.event

import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import org.mongodb.kbson.BsonObjectId
import java.util.Calendar

sealed class AlarmEvent {
    data class DelayAlarm(val id: Int) : AlarmEvent()
    data class CancelAlarm(val id: AlarmRealm) : AlarmEvent()
    data class ChangeAlarmState(val id: BsonObjectId) : AlarmEvent()
    object RestartAlarmState : AlarmEvent()
    data class DeleteLocaleAlarm(val id: Int) : AlarmEvent()
    data class DeleteAlarm(val id: BsonObjectId) : AlarmEvent()
    object SaveAndScheduleAlarm : AlarmEvent()
    data class ScheduleAlarm(val id: AlarmRealm) : AlarmEvent()
    data class UpdateTime(val calendar: Calendar) : AlarmEvent()
}