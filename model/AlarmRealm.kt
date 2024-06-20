package com.yucox.pillpulse.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class AlarmRealm : RealmObject {
    @PrimaryKey
    var id: BsonObjectId = ObjectId()
    var requestCode: Int = 0
    var pillName: String = ""
    var info: String = ""
    var repeating: Int = 1
    var userMail: String? = ""
    var alarmTime: String? = null
    var alarmDate : String? = null
    var onOrOff: Int = 1
}