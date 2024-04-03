package com.yucox.pillpulse

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.yucox.pillpulse.Model.AlarmInfo
import java.util.Calendar

class AlarmUtils(val context: Context) {
    private val alarmManager =
        context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

    fun openTheAlarm(alarmInfo: AlarmInfo) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarmInfo.alarmTime.hours)
        calendar.set(Calendar.MINUTE, alarmInfo.alarmTime.minutes)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context.applicationContext, MyReceiver::class.java)
        intent.putExtra("alarmInfo", alarmInfo)

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext, alarmInfo.requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE
        )

        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            println("kuruldu")
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Lütfen ayarlardan gerekli izinleri veriniz : ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun closeTheAlarm(alarmInfo: AlarmInfo) {
        val intent = Intent(context.applicationContext, MyReceiver::class.java)
        intent.putExtra("alarmInfo", alarmInfo)
        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext, alarmInfo.requestCode, intent,
            PendingIntent.FLAG_MUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun deleteAndClose(alarmInfo: AlarmInfo) {
        val intent = Intent(context.applicationContext, MyReceiver::class.java)
        intent.putExtra("alarmInfo", alarmInfo)
        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext, alarmInfo.requestCode, intent,
            PendingIntent.FLAG_MUTABLE
        )
        alarmManager.cancel(pendingIntent)

        try {
            pendingIntent.cancel()
            println("başarılı")
        } catch (e: Exception) {
            println(e.message)
        }
    }
}