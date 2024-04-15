package com.yucox.pillpulse.ViewModel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig.Flag
import com.yucox.pillpulse.AlarmUtils
import com.yucox.pillpulse.Repository.AlarmRepository
import com.yucox.pillpulse.Repository.PillRepository
import com.yucox.pillpulse.Model.AlarmInfo
import com.yucox.pillpulse.MyReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.Calendar

class AlarmViewModel : ViewModel() {
    private val _alarm = MutableLiveData<AlarmInfo>()
    private val _alarmList = MutableLiveData<ArrayList<AlarmInfo>>()
    private val _error = MutableLiveData<String>()

    val alarmList: LiveData<ArrayList<AlarmInfo>> = _alarmList
    val error: LiveData<String> get() = _error

    fun updateAlarm(newAlarm: AlarmInfo) {
        _alarm.value = newAlarm
    }

    private fun updateAlarmList(newAlarmList: ArrayList<AlarmInfo>) {
        _alarmList.value = newAlarmList
    }

    private fun addAlarmToList() {
        _alarm.value?.let { alarm ->
            val newList = _alarmList.value?.let {
                ArrayList<AlarmInfo>(it)
            }
            newList?.add(alarm)
            _alarmList.value = newList
        }
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
            alarmManager.setExactAndAllowWhileIdle(
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

    fun fetchAlarms() {
        viewModelScope.launch {
            try {
                val result = AlarmRepository().fetchAlarms()
                withContext(Dispatchers.Main) {
                    updateAlarmList(result)
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            }

        }
    }

    fun savePillAlarm(snapKey: String) {
        viewModelScope.launch {
            val (result, exception) = withContext(Dispatchers.IO) {
                PillRepository().savePillAlarm(snapKey, _alarm.value)
            }

            if (result) {
                addAlarmToList()

            } else {
                _error.value = exception
            }
        }
    }
}