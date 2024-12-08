package com.yucox.pillpulse.presentation.effect

sealed class MainEffect {
    data class ShowToast(val message: String) : MainEffect()
    object NavigateToLogin : MainEffect()
    object NavigateToAddReminder : MainEffect()
    object NavigateToAddPill : MainEffect()
    object NavigateToChart : MainEffect()
}