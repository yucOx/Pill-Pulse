package com.yucox.pillpulse.model

import java.io.Serializable
import java.util.Date

data class Pill(
    var id: String = "",
    val drugName: String = "",
    val whenYouTookHour: String? = "",
    val whenYouTookDate: String? = "",
    val userMail: String = "",
    val month: String? = null,
) : Serializable {
}