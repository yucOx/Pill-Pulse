package com.yucox.pillpulse.presentation.state

import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.domain.model.AlarmInfo
import java.util.Calendar

data class AlarmState(
    val alarmList: List<AlarmRealm> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

    )