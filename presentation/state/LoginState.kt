package com.yucox.pillpulse.presentation.state

data class LoginState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)