package com.yucox.pillpulse.model

import java.io.Serializable
import java.util.Date

data class PillTime (val drugName : String = "", val note : String = "", val whenYouTook : Date = Date(), val userMail : String = "", val key : String = "") : Serializable{
}