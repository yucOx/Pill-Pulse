package com.yucox.pillpulse.data.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.domain.manager.IAlarmManager
import com.yucox.pillpulse.domain.model.AlarmInfo
import com.yucox.pillpulse.domain.model.MyReceiver
import com.yucox.pillpulse.util.showToastLong
import com.yucox.pillpulse.util.toAlarmInfo
import com.yucox.pillpulse.util.toTime
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject


class AlarmManagerImpl @Inject constructor(@ApplicationContext val context: Context) :
    IAlarmManager {
    private val alarmManager =
        context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

    override fun scheduleAlarm(alarm: AlarmInfo) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarm.alarmTime.hours)
        calendar.set(Calendar.MINUTE, alarm.alarmTime.minutes)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, MyReceiver::class.java)
        intent.putExtra("alarmInfo", alarm)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            context.showToastLong("Lütfen ayarlardan gerekli izinleri veriniz : ${e.message}")
        }
    }

    override fun scheduleAlarm(alarmRealm: AlarmRealm) {
        val calendar = Calendar.getInstance()
        if (alarmRealm.alarmTime.isNullOrEmpty())
            return
        val time = alarmRealm.alarmTime.toTime()
        calendar.set(Calendar.HOUR_OF_DAY, time.hours)
        calendar.set(Calendar.MINUTE, time.minutes)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, MyReceiver::class.java)
        val alarm = alarmRealm.toAlarmInfo()
        intent.putExtra("alarmInfo", alarm)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmRealm.requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            context.showToastLong("Lütfen ayarlardan gerekli izinleri veriniz : ${e.message}")
        }
    }

    override fun cancelAlarm(alarmRealm: AlarmRealm) {
        val calendar = Calendar.getInstance()
        if (alarmRealm.alarmTime.isNullOrEmpty())
            return

        val time = alarmRealm.alarmTime.toTime()

        calendar.set(Calendar.HOUR_OF_DAY, time.hours)
        calendar.set(Calendar.MINUTE, time.minutes)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, MyReceiver::class.java)

        intent.putExtra("alarmInfo", alarmRealm.toAlarmInfo())
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmRealm.requestCode, intent,
            PendingIntent.FLAG_MUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}