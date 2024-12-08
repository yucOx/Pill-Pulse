package com.yucox.pillpulse.data.remote.repository

import com.yucox.pillpulse.data.mapper.toDomain
import com.yucox.pillpulse.data.mapper.toResponse
import com.yucox.pillpulse.data.remote.api.ApiService
import com.yucox.pillpulse.domain.model.Pill
import com.yucox.pillpulse.domain.repository.IPillRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

class PillRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : IPillRepository {
    override suspend fun fetchPillsPaginated(
        mail: String,
        requestedMonth: Int,
        page: Int,
        limit: Int
    ): Pair<List<Pill>, String?> {
        return withContext(Dispatchers.IO) {
            val response = apiService.getPaginatedPillList(mail, requestedMonth, page, limit)
            Pair(response.body()?.map { it.toDomain() } ?: emptyList(), response.message())
        }
    }

    override suspend fun deletePill(pillId: String, userMail: String) {
        withContext(Dispatchers.IO) {
            apiService.deletePill(
                mail = userMail,
                id = pillId
            )
        }
    }

    override suspend fun saveNewPill(pill: Pill) {
        withContext(Dispatchers.IO) {
            apiService.savePill(pill.userMail, pill.toResponse())
        }
    }

    override suspend fun fetchAllPills(mail: String): Pair<List<Pill>, String?> {
        return withContext(Dispatchers.IO) {
            val response = apiService.getAllPills(mail)
            Pair(response.body()?.map { it.toDomain() } ?: emptyList(), response.message())
        }
    }
}