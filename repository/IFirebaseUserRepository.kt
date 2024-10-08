package com.yucox.pillpulse.repository

import com.yucox.pillpulse.model.UserInfo

interface IFirebaseUserRepository {
    suspend fun fetchMainUserInfo(): UserInfo
    fun signOut()
    fun isAnyoneIn(): Int
    suspend fun logInCheck(mail: String?, pass: String?): Pair<Boolean, String?>
    suspend fun createAccount(mail: String?, pass: String?): Pair<Boolean, String?>
    suspend fun saveUserInfo(user: UserInfo): Pair<Boolean, String?>
}