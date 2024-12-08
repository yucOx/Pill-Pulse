package com.yucox.pillpulse.presentation.viewmodel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.yucox.pillpulse.presentation.effect.MainEffect
import com.yucox.pillpulse.presentation.event.MainEvent
import com.yucox.pillpulse.presentation.state.MainState
import com.yucox.pillpulse.data.remote.repository.FirebaseUserRepositoryImpl
import com.yucox.pillpulse.data.remote.repository.PillRepositoryImpl
import com.yucox.pillpulse.domain.model.Pill
import com.yucox.pillpulse.util.toFormattedDateString
import com.yucox.pillpulse.util.toFormattedTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PillViewModel @Inject constructor(
    private val authRepository: FirebaseUserRepositoryImpl,
    private val pillRepository: PillRepositoryImpl,
) : ViewModel() {
    private val _state = MutableLiveData(MainState())
    val state: LiveData<MainState> = _state

    private val _effect = MutableLiveData<MainEffect>()
    val effect: LiveData<MainEffect> = _effect

    init {
        initialize()
    }

    fun loadInitialForChartActivity() {
        if (_state.value?.allPills.isNullOrEmpty()) {
            loadAllPills()
        } else {
            updateState { it.copy(isLoading = false) }
        }
    }

    fun loadInitialDataForAddTime() {
        updateState { it.copy(pastPills = lastUsedNameOfPills()) }
    }

    private fun initialize() {
        viewModelScope.launch {
            _state.value?.selectedMonth?.let { loadPillsForMonth(it) }
        }
    }

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.MonthSelected -> loadPillsForMonth(event.month)

            is MainEvent.LoadMore -> {
                val monthToLoad = event.month ?: _state.value?.selectedMonth ?: 0
                loadPillsForMonth(monthToLoad)
            }

            is MainEvent.LoadAllPills -> loadAllPills()
            is MainEvent.DeletePill -> deletePill(event.id)
            is MainEvent.LogOut -> handleLogOut()
            is MainEvent.AddReminder -> navigateToAddReminder()
            is MainEvent.AddPill -> navigateToAddPill()
            is MainEvent.OpenChart -> navigateToChart()
            is MainEvent.SavePill -> savePillToData(event.pillName)
        }
    }

    private fun navigateToChart() {
        _effect.value = MainEffect.NavigateToChart
    }

    private fun navigateToAddPill() {
        _effect.value = MainEffect.NavigateToAddPill
    }

    private fun navigateToAddReminder() {
        _effect.value = MainEffect.NavigateToAddReminder
    }

    private fun handleLogOut() {
        authRepository.signOut()
        _effect.value = MainEffect.NavigateToLogin
    }


    private fun updateState(update: (MainState) -> MainState) {
        _state.value = update(_state.value ?: MainState())
    }


    private fun shouldSkipLoading(): Boolean {
        return (_state.value?.isLastPage == true || _state.value?.isLoading == true)
    }

    private fun loadAllPills() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            try {
                val (pills, message) = pillRepository.fetchAllPills(authRepository.getCurrentUserMail())
                updateState {
                    it.copy(
                        isLoading = false,
                        allPills = pills,
                        error = message,
                    )
                }
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, error = e.message) }
                _effect.value = MainEffect.ShowToast("Beklenmedik bir hata ile karşılaşıldı")
            }

        }
    }

    private fun loadPillsForMonth(requestedMonth: Int) {
        viewModelScope.launch {
            try {
                when {
                    isAllPillsLoaded() -> {
                        loadFromLoadedPills(requestedMonth)
                        return@launch
                    }

                    isMonthChanged(requestedMonth) -> resetState(requestedMonth)
                    shouldSkipLoading() -> return@launch
                }
                fetchAndUpdatePills(requestedMonth)
            } catch (e: Exception) {
                updateState { it.copy(isLoading = false, error = e.message) }
                _effect.value = MainEffect.ShowToast("Beklenmedik bir hata ile karşılaşıldı")
            }
        }
    }

    private fun isAllPillsLoaded(): Boolean {
        return _state.value?.allPills?.isNotEmpty() == true
    }

    private fun loadFromLoadedPills(requestedMonth: Int) {
        if (!isMonthChanged(requestedMonth)) return
        val filteredPills =
            if (requestedMonth == 0) _state.value?.allPills else _state.value?.allPills?.filter { it.month == requestedMonth.toString() }
        resetState(requestedMonth)
        updateState {
            it.copy(
                selectedPills = createOrAddToPills(filteredPills ?: emptyList()),
                isLastPage = true,
                selectedMonth = requestedMonth
            )
        }
    }

    private suspend fun fetchAndUpdatePills(requestedMonth: Int) {
        updateState { it.copy(isLoading = true) }
        val currentPage = _state.value?.page ?: 0
        val (pills, message) = pillRepository.fetchPillsPaginated(
            mail = authRepository.getCurrentUserMail(),
            page = currentPage,
            limit = 20,
            requestedMonth = requestedMonth
        )
        updateState {
            it.copy(
                isLoading = false,
                selectedPills = createOrAddToPills(pills),
                selectedMonth = requestedMonth,
                page = currentPage + pills.size,
                error = message,
                isLastPage = pills.size < 10
            )
        }
    }

    private fun isMonthChanged(requestedMonth: Int): Boolean {
        return _state.value?.selectedMonth != requestedMonth
    }

    private fun resetState(requestedMonth: Int) {
        updateState {
            it.copy(
                selectedPills = emptyList(),
                page = 0,
                isLastPage = false,
                selectedMonth = requestedMonth,
            )
        }
    }

    private fun createOrAddToPills(pills: List<Pill>): List<Pill> {
        val currentList = _state.value?.selectedPills ?: emptyList()
        return if (currentList.isEmpty() && pills.isEmpty()) {
            listOf(
                createPill("Bu tarihte hiç"),
                createPill("ilaç eklemediniz")
            )
        } else {
            currentList + pills
        }
    }

    fun savePillToData(pillName: String) {
        viewModelScope.launch {
            try {
                val currentState = _state.value ?: MainState()
                if (currentState.isLoading) return@launch
                if (pillName.isBlank()) {
                    _effect.value = MainEffect.ShowToast("İlaç adı boş olamaz")
                    return@launch
                }
                pillRepository.saveNewPill(createPill(pillName))
                resetState(currentState.selectedMonth)
                if (isAllPillsLoaded())
                    loadAllPills()
                else
                    loadPillsForMonth(currentState.selectedMonth)
            } catch (e: Exception) {
                _effect.value = MainEffect.ShowToast("Beklenmedik bir hata ile karşılaşıldı")
            }
        }
    }


    private fun createPill(pillName: String): Pill {
        return Pill(
            drugName = pillName,
            whenYouTookDate = Date().toFormattedDateString(),
            whenYouTookHour = Date().toFormattedTimeString(),
            month = _state.value?.selectedMonth.toString(),
            userMail = authRepository.getCurrentUserMail(),
            id = "1",
            _id = ""
        )
    }

    private fun deletePill(id: String) {
        viewModelScope.launch {
            pillRepository.deletePill(id, authRepository.getCurrentUserMail())
        }
    }

    private fun lastUsedNameOfPills(): MutableList<String> {
        val currentState = _state.value ?: MainState()
        when {
            isAllPillsLoaded() -> return currentState.allPills
                .distinctBy { it.drugName.lowercase() }
                .map { it.drugName }
                .toMutableList().ifEmpty { mutableListOf("İlaç adı bulunamadı") }

            else -> return currentState.selectedPills
                .distinctBy { it.drugName.lowercase() }
                .map { it.drugName }
                .toMutableList().ifEmpty { mutableListOf("İlaç adı bulunamadı") }
        }
    }

    fun setupChart(requestedMonth: Int): PieDataSet {
        val pillsCountMap = mutableMapOf<String, Float>()
        val colorPallet = (ColorTemplate.VORDIPLOM_COLORS + ColorTemplate.JOYFUL_COLORS)
            .toCollection(ArrayList())
        val executingList = if (_state.value?.allPills.isNullOrEmpty()) _state.value?.selectedPills
            ?: emptyList() else _state.value?.allPills ?: emptyList()
        when {
            requestedMonth == 0 -> {
                val tempPillList =
                    executingList.map { it.drugName.capitalize() }.toMutableList()
                        .ifEmpty { mutableListOf("Bu tarihte hiç ilaç eklemediniz") }
                tempPillList.forEach { drugName ->
                    pillsCountMap[drugName] = pillsCountMap.getOrDefault(drugName, 0f) + 1f
                }
            }

            else -> {
                val tempPillList =
                    executingList.filter { it.month == requestedMonth.toString() }
                        .map { it.drugName.capitalize() }.toMutableList()
                        .ifEmpty { mutableListOf("Bu tarihte hiç ilaç eklemediniz") }
                tempPillList.forEach { drugName ->
                    pillsCountMap[drugName] = pillsCountMap.getOrDefault(drugName, 0f) + 1f
                }
            }
        }
        val pieEntry = pillsCountMap.map { (name, count) -> PieEntry(count, name) }
        val pieDataSet = PieDataSet(pieEntry, "").apply {
            colors = colorPallet
            valueTextColor = Color.BLACK
            valueTextSize = 13f
        }
        return pieDataSet

    }
}