package com.yucox.pillpulse.data.remote.api

data class PillResponse(
    val _id: String = "",
    val id: String = "",
    val drugName: String = "",
    val whenYouTookHour: String? = "",
    val whenYouTookDate: String? = "",
    val userMail: String = "",
    val month: String? = null
)