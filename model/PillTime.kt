package com.yucox.pillpulse.model

import java.io.Serializable
import java.util.Date

data class PillTime(
    val drugName: String = "",
    var note: String = "",
    val whenYouTook: Date = Date(),
    val userMail: String = "",
    var key: String = ""
) : Serializable {
}