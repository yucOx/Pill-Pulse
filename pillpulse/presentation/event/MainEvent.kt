package com.yucox.pillpulse.presentation.event

sealed class MainEvent {
    data class MonthSelected(val month: Int) : MainEvent()
    object LogOut : MainEvent()
    object AddReminder : MainEvent()
    object AddPill : MainEvent()
    object OpenChart : MainEvent()
    data class DeletePill(val id: String) : MainEvent()
    object LoadAllPills : MainEvent()
    data class LoadMore(val month: Int? = null) : MainEvent()
    data class SavePill(val pillName: String) : MainEvent()

}