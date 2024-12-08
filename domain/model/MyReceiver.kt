package com.yucox.pillpulse.domain.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import com.yucox.pillpulse.R
import com.yucox.pillpulse.data.manager.AlarmManagerImpl
import com.yucox.pillpulse.presentation.view.alarm.AlarmOnActivity
import com.yucox.pillpulse.util.showToast
import com.yucox.pillpulse.util.showToastLong
import javax.inject.Inject

class MyReceiver @Inject constructor(private val alarmManager: AlarmManagerImpl) :
    BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "area 31"
        private const val CHANNEL_NAME = "Pill Pulse"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { safeContext ->
            intent?.let { safeIntent ->
                handleAlarm(safeContext, safeIntent)
            }
        }
    }

    private fun handleAlarm(context: Context, intent: Intent) {
        val alarmInfo = getAlarmInfo(intent)
        val mediaPlayer = createMediaPlayer(context)

        showInitialNotification(context)
        createNotificationChannel(context)
        startAlarm(context, mediaPlayer)
        setupAlarmUtils(context, alarmInfo)
        createAndShowNotification(context, alarmInfo, mediaPlayer)
    }

    private fun getAlarmInfo(intent: Intent): AlarmInfo {
        return intent.getSerializableExtra("alarmInfo") as AlarmInfo
    }

    private fun createMediaPlayer(context: Context): MediaPlayer {
        return MediaPlayer.create(context, R.raw.alarm_sound)
    }

    private fun showInitialNotification(context: Context) {
        context.showToast("Alarm çalıyor")
    }

    private fun startAlarm(context: Context, mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

    private fun setupAlarmUtils(context: Context, alarmInfo: AlarmInfo) {
        alarmManager.scheduleAlarm(alarmInfo)
    }

    private fun createAndShowNotification(
        context: Context,
        alarmInfo: AlarmInfo,
        mediaPlayer: MediaPlayer
    ) {
        val pendingIntent = createPendingIntent(context, alarmInfo, mediaPlayer)
        val notification = buildNotification(context, alarmInfo, pendingIntent)
        showNotification(context, notification)
        context.showToastLong("Alarm çalıyor")
    }

    private fun createPendingIntent(
        context: Context,
        alarmInfo: AlarmInfo,
        mediaPlayer: MediaPlayer
    ): PendingIntent {
        val notificationIntent = Intent(context, AlarmOnActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("alarmInfo", alarmInfo)
            putExtra("mpUri", mediaPlayer.toString())
        }

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(
        context: Context,
        alarmInfo: AlarmInfo,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(androidx.core.R.drawable.notification_action_background)
            .setContentTitle(CHANNEL_NAME)
            .setContentText(alarmInfo.pillName)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    private fun showNotification(context: Context, builder: NotificationCompat.Builder) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}