package com.yucox.pillpulse.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.yucox.pillpulse.model.ILocalePillRepository
import com.yucox.pillpulse.model.PillRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class LocalePillRepository @Inject constructor(
    private val realm: Realm
) :
    ILocalePillRepository {
    override fun fetchPills(): LiveData<List<PillRealm>> {
        return realm.query<PillRealm>().find()
            .asFlow()
            .map { it.list }
            .asLiveData()
    }

    override suspend fun removePill(id: ObjectId) {
        realm.write {
            val queriedPill = query<PillRealm>("id = $0", id).first().find()
            queriedPill?.let { delete(it) }
        }
    }

    override suspend fun savePill(pill: PillRealm) {
        realm.write {
            copyToRealm(pill)
        }
    }
}