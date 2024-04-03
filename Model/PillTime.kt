package com.yucox.pillpulse.Model

import java.io.Serializable
import java.util.Date

data class PillTime (val drugName : String = "", var note : String = "", val whenYouTook : Date = Date(), val userMail : String = "", val key : String = "") : Serializable{
}