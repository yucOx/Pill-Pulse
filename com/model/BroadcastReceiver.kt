package com.yucox.pillpulse.model

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.yucox.pillpulse.R
import com.yucox.pillpulse.activity.AlarmOnActivity
import java.util.Calendar


class BroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        var alarmInfo = p1?.getSerializableExtra("alarmInfo") as AlarmInfo


        Toast.makeText(p0,"Alarm çalıyor",Toast.LENGTH_SHORT).show()
        createNotificationChannel(p0!!)
        var mp : MediaPlayer
        mp =  MediaPlayer.create(p0, R.raw.alarm_sound)
        mp.start()
        println("burada")
        //cancelAlarmAndSetAgain(p0,alarmInfo)

        val notificationIntent = Intent(p0, AlarmOnActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            p0, requestCode, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(p0!!, "channelId")
            .setSmallIcon(androidx.core.R.drawable.notification_action_background)
            .setContentTitle("PillPulse")
            .setContentText("${alarmInfo.pillName}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())

        Toast.makeText(p0,"Alarm çalıyor",Toast.LENGTH_LONG).show()
    }

    private fun cancelAlarmAndSetAgain(p0: Context, alarmInfo: AlarmInfo) {
        val intent = Intent(p0.applicationContext, BroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(p0, alarmInfo.requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = p0.getSystemService(ALARM_SERVICE) as AlarmManager
        var calendar = Calendar.getInstance()
        println(pendingIntent)
        alarmManager.cancel(pendingIntent)
        println(pendingIntent)
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,pendingIntent)

    }

    private fun createNotificationChannel(p0: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channelId",
                "Channel Name",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                p0?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}