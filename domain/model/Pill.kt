package com.yucox.pillpulse.domain.model

import org.mongodb.kbson.ObjectId
import java.io.Serializable

data class Pill(
    val _id : String,
    val id: String,
    val drugName: String,
    val whenYouTookHour: String?,
    val whenYouTookDate: String?,
    val userMail: String,
    val month: String?
)