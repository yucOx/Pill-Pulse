package com.yucox.pillpulse.presentation.state

import android.icu.util.Calendar
import com.yucox.pillpulse.domain.model.Pill

data class MainState(
    val isLoading: Boolean = false,
    val selectedPills: List<Pill> = emptyList(),
    val allPills : List<Pill> = emptyList(),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val error: String? = null,
    val isLastPage: Boolean = false,
    val page: Int = 0,
    val pastPills : List<String> = emptyList()
)