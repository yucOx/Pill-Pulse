package com.yucox.pillpulse.domain.repository

interface IFirebaseUserRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    suspend fun isLoggedIn(): Boolean
    fun getCurrentUserEmail(): String
    fun signOut()
    fun getCurrentUserMail(): String
}
