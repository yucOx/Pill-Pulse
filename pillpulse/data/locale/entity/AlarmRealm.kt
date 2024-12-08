package com.yucox.pillpulse.data.locale.entity

import com.yucox.pillpulse.domain.model.AlarmInfo
import com.yucox.pillpulse.util.toTime
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.util.Date

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
