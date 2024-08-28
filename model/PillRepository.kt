package com.yucox.pillpulse.model

import retrofit2.Response

interface PillRepository {
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