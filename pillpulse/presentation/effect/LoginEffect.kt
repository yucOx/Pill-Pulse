package com.yucox.pillpulse.presentation.effect

sealed class LoginEffect {
    object NavigateToMain : LoginEffect()
    object NavigateToRegister : LoginEffect()
    object NavigateBack : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
}