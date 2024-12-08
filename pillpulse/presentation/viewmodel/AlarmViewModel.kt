package com.yucox.pillpulse.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucox.pillpulse.domain.model.AlarmInfo
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.data.locale.repository.LocaleAlarmRepositoryImpl
import com.yucox.pillpulse.data.manager.AlarmManagerImpl
import com.yucox.pillpulse.data.remote.repository.FirebaseUserRepositoryImpl
import com.yucox.pillpulse.presentation.effect.AlarmEffect
import com.yucox.pillpulse.presentation.event.AlarmEvent
import com.yucox.pillpulse.presentation.state.AlarmState
import com.yucox.pillpulse.util.toAlarmInfo
import com.yucox.pillpulse.util.toFormattedDateString
import com.yucox.pillpulse.util.toFormattedTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val _localeAlarmRepositoryImpl: LocaleAlarmRepositoryImpl,
    private val userRepository: FirebaseUserRepositoryImpl,
    private val alarmManager: AlarmManagerImpl
) : ViewModel() {
    private var calendar = java.util.Calendar.getInstance()
    private var currentAlarm = AlarmInfo()
    private var currentAlarmRealm = AlarmRealm()

    private val _state = MutableLiveData<AlarmState>()
    val state: LiveData<AlarmState> get() = _state

    private val _effect = MutableLiveData<AlarmEffect>()
    val effect: LiveData<AlarmEffect> get() = _effect


    init {
        loadAlarmList()
    }

    private fun loadAlarmList() {
        val alarmList = loadLocaleAlarms()
        println(alarmList)
        updateState { it.copy(alarmList = alarmList) }
        reOpenAlarms()
    }

    fun onEvent(event: AlarmEvent) {
        when (event) {
            is AlarmEvent.DelayAlarm -> delayOneDay()
            is AlarmEvent.CancelAlarm -> cancelAlarm(event.id)
            is AlarmEvent.ScheduleAlarm -> scheduleAlarm(event.id)
            is AlarmEvent.DeleteAlarm -> deleteAlarmLocale(event.id)
            is AlarmEvent.DeleteLocaleAlarm -> deleteAlarmLocale(
                BsonObjectId(
                    event.id.toString()
                )
            )

            is AlarmEvent.RestartAlarmState -> reOpenAlarms()
            is AlarmEvent.SaveAndScheduleAlarm -> setAndSaveAlarm()
            is AlarmEvent.ChangeAlarmState -> changeAlarmStatusLocale(event.id)
            is AlarmEvent.UpdateTime -> updateCalendar(event.calendar)
        }
    }

    private fun updateCalendar(calendar: Calendar) {
        this.calendar = calendar
    }

    private fun updateState(update: (AlarmState) -> AlarmState) {
        _state.value = update(_state.value ?: AlarmState())
    }

    private fun loadLocaleAlarms(): List<AlarmRealm> {
        return _localeAlarmRepositoryImpl.fetchAlarms()
    }

    fun initAlarmObjects(
        pillName: String,
        repeating: Int
    ) {
        val requestCode = System.currentTimeMillis().toInt()
        currentAlarmRealm = currentAlarmRealm.apply {
            this.pillName = pillName
            this.requestCode = requestCode
            this.repeating = repeating
            this.userMail = userRepository.getCurrentUserMail()
            this.onOrOff = 1
            this.alarmTime = calendar.time.toFormattedTimeString()
            this.alarmDate = calendar.time.toFormattedDateString()
        }

        currentAlarm = currentAlarmRealm.toAlarmInfo()
    }

    private fun delayOneDay() {
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        _effect.value = AlarmEffect.ShowToast("Hatırlatıcı 1 gün ertelendi")
    }

    private fun setAndSaveAlarm() {
        if (currentAlarm.pillName.isBlank()) {
            _effect.value = AlarmEffect.ShowToast("İlaç adı boş olamaz.")
            return
        }
        if (skipToday())
            delayOneDay()
        currentAlarm.alarmTime = calendar.time
        alarmManager.scheduleAlarm(currentAlarmRealm)
        createNewAlarmLocale(currentAlarmRealm)
        _effect.value = AlarmEffect.ShowToast("Hatırlatıcı ayarlandı")
        val updatedAlarms = loadLocaleAlarms()
        updateState { it.copy(alarmList = updatedAlarms) }
    }

    private fun skipToday(): Boolean {
        return calendar.timeInMillis <= System.currentTimeMillis()
    }

    fun reOpenAlarms() {
        val restartRequiredList = _state.value?.alarmList?.filter { it.onOrOff == 1 } ?: emptyList()
        restartRequiredList.forEach { alarm ->
            alarmManager.scheduleAlarm(alarm)
        }
    }

    fun scheduleAlarm(alarm: AlarmRealm) {
        alarmManager.scheduleAlarm(alarm)
    }

    fun scheduleAlarm(alarm: AlarmInfo) {
        alarmManager.scheduleAlarm(alarm)
    }

    fun cancelAlarm(alarm: AlarmRealm) {
        alarmManager.cancelAlarm(alarm)
    }


    fun deleteAlarmLocale(alarmId: ObjectId) {
        viewModelScope.launch {
            _localeAlarmRepositoryImpl.removeAlarm(alarmId)
        }
    }

    private fun createNewAlarmLocale(alarm: AlarmRealm) {
        viewModelScope.launch {
            try {
                _localeAlarmRepositoryImpl.createNewAlarm(alarm)
                println("başarılı")
            } catch (e: Exception) {
                println("${e.message}")
            }
        }
    }

    fun changeAlarmStatusLocale(alarmId: ObjectId) {
        viewModelScope.launch {
            _localeAlarmRepositoryImpl.changeAlarmStatus(alarmId)
        }

    }
}