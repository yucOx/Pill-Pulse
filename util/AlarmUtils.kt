package com.yucox.pillpulse.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yucox.pillpulse.model.MyReceiver
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.model.AlarmRealm
import java.text.SimpleDateFormat
import java.util.Calendar

class AlarmUtils(val context: Context) {
    private val alarmManager =
        context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
    private val _shf = SimpleDateFormat("HH:mm")

    fun openTheAlarm(alarmRealm: AlarmRealm) {
        val calendar = Calendar.getInstance()
        if (alarmRealm.alarmTime.isNullOrEmpty())
            return
        val time = _shf.parse(alarmRealm.alarmTime)
        calendar.set(Calendar.HOUR_OF_DAY, time.hours)
        calendar.set(Calendar.MINUTE, time.minutes)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context.applicationContext, MyReceiver::class.java)
        val alarm = convertToAlarmObject(alarmRealm, calendar)
        intent.putExtra("alarmInfo", alarm)

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
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
            Toast.makeText(
                context,
                "Lütfen ayarlardan gerekli izinleri veriniz : ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun openTheAlarm(alarm: AlarmInfo) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarm.alarmTime.hours)
        calendar.set(Calendar.MINUTE, alarm.alarmTime.minutes)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context.applicationContext, MyReceiver::class.java)
        intent.putExtra("alarmInfo", alarm)

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
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
            Toast.makeText(
                context,
                "Lütfen ayarlardan gerekli izinleri veriniz : ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun closeTheAlarm(alarmRealm: AlarmRealm) {
        val calendar = Calendar.getInstance()
        if (alarmRealm.alarmTime.isNullOrEmpty())
            return

        val time = _shf.parse(alarmRealm.alarmTime)

        calendar.set(Calendar.HOUR_OF_DAY, time.hours)
        calendar.set(Calendar.MINUTE, time.minutes)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context.applicationContext, MyReceiver::class.java)
        val alarm = convertToAlarmObject(alarmRealm, calendar)

        intent.putExtra("alarmInfo", alarm)
        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext, alarmRealm.requestCode, intent,
            PendingIntent.FLAG_MUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun convertToAlarmObject(alarmRealm: AlarmRealm, calendar: Calendar): AlarmInfo {
        return AlarmInfo(
            requestCode = alarmRealm.requestCode,
            pillName = alarmRealm.pillName,
            info = "",
            repeating = 1,
            userMail = alarmRealm.userMail,
            alarmLocation = alarmRealm.id.toHexString(),
            alarmTime = calendar.time,
            onOrOff = 1
        )
    }
}