package com.yucox.pillpulse.model

import io.realm.kotlin.types.RealmObject
import org.mongodb.kbson.ObjectId

class PillRealm : RealmObject {
    var id: ObjectId = ObjectId()
    var drugName: String = ""
    var note: String = ""
    var tokeTime: String = ""
    var tokeDate : String = ""
    var userMail: String = ""
}