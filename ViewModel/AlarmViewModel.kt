package com.yucox.pillpulse.ViewModel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yucox.pillpulse.AlarmUtils
import com.yucox.pillpulse.Repository.AlarmRepository
import com.yucox.pillpulse.Repository.PillRepository
import com.yucox.pillpulse.Model.AlarmInfo
import com.yucox.pillpulse.MyReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AlarmViewModel : ViewModel() {
    private val _alarm = MutableLiveData<AlarmInfo>()
    private val _alarmList = MutableLiveData<ArrayList<AlarmInfo>>()

    val alarm: LiveData<AlarmInfo> = _alarm
    val alarmList: LiveData<ArrayList<AlarmInfo>> = _alarmList

    fun updateAlarm(newAlarm: AlarmInfo) {
        _alarm.value = newAlarm
    }

    private fun updateAlarmList(newAlarmList: ArrayList<AlarmInfo>) {
        _alarmList.value = newAlarmList
    }

    fun reOpenAlarms(context: Context) {
        val alarmUtils = AlarmUtils(context)
        _alarmList.value?.let {
            for (alarm in it) {
                if (alarm.onOrOff == 1) {
                    alarmUtils.openTheAlarm(alarm)
                }
            }
        }
    }

    fun setAlarm(context: Context, calendar: Calendar) {
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(
            context.applicationContext,
            MyReceiver::class.java
        )
        intent.putExtra("alarmInfo", _alarm.value)
        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            _alarm.value!!.requestCode,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            println(e.message)
            Toast.makeText(
                context,
                "Lütfen ayarlardan gerekli izinleri veriniz : ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun fetchAlarms(viewModel: AlarmViewModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = AlarmRepository().fetchAlarms()
            withContext(Dispatchers.Main) {
                updateAlarmList(result)
            }
        }
    }

    suspend fun savePillAlarm(snapKey: String, viewModel: AlarmViewModel): Boolean {
        return withContext(Dispatchers.IO) {
            PillRepository().savePillAlarm(snapKey, viewModel)
        }
    }
}