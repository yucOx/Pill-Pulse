package com.yucox.pillpulse.presentation.event

sealed class LoginEvent {
    data class Login(val email: String, val password: String) : LoginEvent()
    data class Register(val email: String, val password: String) : LoginEvent()
    object CheckLoggedIn : LoginEvent()
    object NavigateToRegister : LoginEvent()
    object NavigateBack : LoginEvent()
}
