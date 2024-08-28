package com.yucox.pillpulse.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.yucox.pillpulse.R
import com.yucox.pillpulse.utils.AlarmUtils
import com.yucox.pillpulse.view.AlarmOnActivity


class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val alarmInfo = intent?.getSerializableExtra("alarmInfo") as AlarmInfo
        Toast.makeText(context, "Alarm çalıyor", Toast.LENGTH_SHORT).show()
        createNotificationChannel(context!!)
        val mp: MediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        mp.start()
        val openAlarmOrClose = AlarmUtils(context.applicationContext)
        openAlarmOrClose.openTheAlarm(alarmInfo)

        val notificationIntent = Intent(context, AlarmOnActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val requestCode = System.currentTimeMillis().toInt()
        notificationIntent.putExtra("alarmInfo", alarmInfo)
        notificationIntent.putExtra("mpUri", mp.toString())

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "area 31")
            .setSmallIcon(androidx.core.R.drawable.notification_action_background)
            .setContentTitle("Pill Pulse")
            .setContentText(alarmInfo.pillName)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val random = System.currentTimeMillis().toInt()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(random, builder.build())
        Toast.makeText(context, "Alarm çalıyor", Toast.LENGTH_LONG).show()
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            "area 31",
            "Pill Pulse",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}