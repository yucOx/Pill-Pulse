package com.yucox.pillpulse.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class OpenAlarmOnRestart (var context: Context) {
    var database = FirebaseDatabase.getInstance().getReference("Alarms")
    var alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

    fun closeTheAlarm(alarmInfo: AlarmInfo) {
        val intent = Intent(context.applicationContext, BroadcastReceiver::class.java)
        intent.putExtra("alarmInfo",alarmInfo)
        var pendingIntent = PendingIntent.getBroadcast(context.applicationContext,alarmInfo.requestCode,intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    fun openTheAlarm(alarmInfo : AlarmInfo) {
        var calendar2 = Calendar.getInstance()
        calendar2.set(Calendar.HOUR_OF_DAY,alarmInfo.alarmTime.hours)
        calendar2.set(Calendar.MINUTE,alarmInfo.alarmTime.minutes)
        if (calendar2.timeInMillis <= System.currentTimeMillis()) {
            calendar2.add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context.applicationContext, BroadcastReceiver::class.java)
        intent.putExtra("alarmInfo",alarmInfo)
        var pendingIntent = PendingIntent.getBroadcast(context.applicationContext,alarmInfo.requestCode,intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar2.timeInMillis, AlarmManager.INTERVAL_DAY,pendingIntent)
    }
}