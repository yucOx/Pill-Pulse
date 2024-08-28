package com.yucox.pillpulse.repository

import com.yucox.pillpulse.model.ApiService
import com.yucox.pillpulse.model.Pill
import com.yucox.pillpulse.model.PillRepository
import javax.inject.Inject

class PillRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PillRepository {
    override suspend fun fetchPillsPaginated(
        mail: String,
        requestedMonth: Int,
        page: Int,
        limit: Int
    ): Pair<List<Pill>?, String?> {
        val response = apiService.getPaginatedPillList(
            mail = mail,
            requestedMonth = requestedMonth,
            page = page,
            limit = limit
        )
        return response.body() to response.message()
    }


    override suspend fun deletePill(pillId: String, userMail: String) {
        apiService.deletePill(
            mail = userMail,
            id = pillId
        )
    }

    override suspend fun saveNewPill(pill: Pill) {
        apiService.savePill(pill.userMail, pill)
    }

    override suspend fun fetchAllPills(mail: String): Pair<List<Pill>?, String?> {
        val response = apiService.getAllPills(mail)
        return response.body() to response.message()
    }
}