package com.yucox.pillpulse.model

import java.io.Serializable
import java.util.Date

class AlarmInfo(
    var requestCode: Int = 0,
    var pillName: String = "",
    var info: String = "",
    var repeating: Int = 0,
    var userMail: String? = "",
    var alarmLocation: String? = "",
    var alarmTime: Date = Date(),
    var onOrOff: Int = 1
) : Serializable {
}