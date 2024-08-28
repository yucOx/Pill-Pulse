package com.yucox.pillpulse.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.yucox.pillpulse.utils.AlarmUtils
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.model.AlarmRealm
import com.yucox.pillpulse.receiver.MyReceiver
import com.yucox.pillpulse.repository.LocaleAlarmRepository
import com.yucox.pillpulse.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val _localeAlarmRepository: LocaleAlarmRepository,
    private val _auth: FirebaseAuth
) : ViewModel() {
    var calendar = java.util.Calendar.getInstance()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _alarm = MutableLiveData<AlarmInfo>()
    private val _alarmRealm = MutableLiveData<AlarmRealm>()

    var rmAlarmList: LiveData<List<AlarmRealm>> = _localeAlarmRepository.fetchAlarms()

    fun initAlarmObjects(
        pillName: String,
        repeating: Int
    ) {
        val requestCode = System.currentTimeMillis().toInt()
        _alarm.value = AlarmInfo(
            requestCode = requestCode,
            pillName = pillName,
            info = "",
            repeating = repeating,
            userMail = _auth.currentUser?.email.toString(),
            alarmLocation = _alarmRealm.value?.id?.toHexString(),
            alarmTime = calendar.time,
            onOrOff = 1
        )
        _alarmRealm.value = AlarmRealm().apply {
            this.pillName = pillName
            this.requestCode = requestCode
            this.repeating = 1
            this.userMail = _auth.currentUser?.email.toString()
            this.onOrOff = 1
            this.alarmTime = TimeUtils.toStringClock(calendar.time)
            this.alarmDate = TimeUtils.toStringCalendar(calendar.time)
        }
    }

    private fun delayToAlarm() {
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        messageChannel("Hatırlatıcı yarına ayarlandı")
    }

    fun setAlarm(context: Context) {
        if (calendar.timeInMillis <= System.currentTimeMillis())
            delayToAlarm()

        _alarmRealm.value?.let {
            createNewAlarmLocale(it)
        }

        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(
            context.applicationContext,
            MyReceiver::class.java
        )
        _alarm.value?.let { alarm ->
            intent.putExtra("alarmInfo", _alarm.value)
            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                alarm.requestCode,
                intent,
                PendingIntent.FLAG_MUTABLE
            )
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                messageChannel("Hatırlatıcı ayarlandı")

            } catch (e: SecurityException) {
                messageChannel("Lütfen ayarlardan gerekli izinleri veriniz : ${e.message}")
            }
        }
    }

    fun reOpenAlarms(context: Context) {
        val alarmUtils = AlarmUtils(context)
        rmAlarmList.value?.let { alarmRealm ->
            for (alarm in alarmRealm) {
                if (alarm.onOrOff == 0) {
                    continue
                }
                alarmUtils.openTheAlarm(alarm)
            }
        }
    }

    private fun messageChannel(message: String) {
        _message.value = message
        _message.value = ""
    }

    fun deleteAlarmLocale(alarmId: ObjectId) {
        viewModelScope.launch {
            _localeAlarmRepository.removeAlarm(alarmId)
        }
    }

    private fun createNewAlarmLocale(alarm: AlarmRealm) {
        viewModelScope.launch {
            _localeAlarmRepository.createNewAlarm(alarm)
        }
    }

    fun changeAlarmStatusLocale(alarmId: ObjectId) {
        viewModelScope.launch {
            _localeAlarmRepository.changeAlarmStatus(alarmId)
        }
    }
}