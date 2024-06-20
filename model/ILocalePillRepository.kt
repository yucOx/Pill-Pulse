package com.yucox.pillpulse.model

import androidx.lifecycle.LiveData
import org.mongodb.kbson.ObjectId

interface ILocalePillRepository {
    fun fetchPills(): LiveData<List<PillRealm>>
    suspend fun removePill(id: ObjectId)
    suspend fun savePill(pill: PillRealm)
}