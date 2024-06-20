package com.yucox.pillpulse.model

import com.google.android.gms.tasks.Task


interface IFirebasePillRepository {
    suspend fun fetchPillsInfo(): ArrayList<PillTime>
    suspend fun deletePill(pillInfo: PillTime): Pair<Boolean, String?>
    suspend fun saveNewPill(pill: String, note: String): Boolean
    fun savePillWithSpecifiedTime(pill: PillTime): Task<Boolean>
}