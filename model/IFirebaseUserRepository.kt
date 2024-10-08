package com.yucox.pillpulse.model

interface IFirebaseUserRepository {
    fun signOut()
    fun isAnyoneIn(): Int
    suspend fun logInCheck(mail: String?, pass: String?): Pair<Boolean, String?>
    suspend fun createAccount(mail: String?, pass: String?): Pair<Boolean, String?>
}