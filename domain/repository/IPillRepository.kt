package com.yucox.pillpulse.domain.repository

import com.yucox.pillpulse.domain.model.Pill
import org.mongodb.kbson.ObjectId

interface IPillRepository {
    suspend fun deletePill(
        pillId: String,
        userMail: String
    )
    suspend fun saveNewPill(pill: Pill)
    suspend fun fetchAllPills(mail: String): Pair<List<Pill>?, String?>
    suspend fun fetchPillsPaginated(
        mail: String,
        requestedMonth: Int,
        page: Int,
        limit: Int
    ): Pair<List<Pill>?, String?>
}