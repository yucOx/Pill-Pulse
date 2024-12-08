package com.yucox.pillpulse.presentation.effect

sealed class AlarmEffect {
    data class ShowToast(val message: String) : AlarmEffect()
}