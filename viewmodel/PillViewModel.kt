package com.yucox.pillpulse.viewmodel

import android.content.Context
import android.graphics.Color
import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.yucox.pillpulse.repository.PillRepositoryImpl
import com.yucox.pillpulse.model.Pill
import com.yucox.pillpulse.util.TimeUtils
import com.yucox.pillpulse.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class PillViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val pillRepository: PillRepositoryImpl,
) : ViewModel() {
    private val currentUserMail = auth.currentUser?.email.toString()
    private val calendar = java.util.Calendar.getInstance()
    private val monthOfToday = (calendar.get(Calendar.MONTH) + 1).toString()
    var selectedMonth = monthOfToday.toInt()

    private val _allPills = MutableLiveData<MutableList<Pill>>()
    val allPills: LiveData<MutableList<Pill>> get() = _pillListForOneMonth

    private val _pillListForOneMonth = MutableLiveData<MutableList<Pill>>()
    val pillListForOneMonth: LiveData<MutableList<Pill>> get() = _pillListForOneMonth

    private val _onProcess = MutableLiveData<Boolean>(false)
    val onProcess: LiveData<Boolean> get() = _onProcess

    private var _page: Int = 0

    private val _message = MutableLiveData<String>()
    val message = MutableLiveData<String>()

    init {
        viewModelScope.launch {
            _onProcess.value = true
            launch { getPillsPaginated(monthOfToday.toInt()) }
            launch { getAllPills() }
        }
    }

    private suspend fun getAllPills() {
        val (data, message) = withContext(Dispatchers.IO) {
            pillRepository.fetchAllPills(currentUserMail)
        }
        if (data != null) {
            _allPills.value = data.toMutableList()
        }
        if (!message.isNullOrEmpty()) {
            _message.value = message.toString()
        }
        _onProcess.value = false
    }

    suspend fun getPillsPaginated(
        requestedMonth: Int,
    ) {
        _onProcess.value = true
        if (requestedMonth != selectedMonth) {
            _page = 0
            selectedMonth = requestedMonth
            _pillListForOneMonth.value = mutableListOf()
        }
        val (data, message) = withContext(Dispatchers.IO) {
            pillRepository.fetchPillsPaginated(
                mail = currentUserMail,
                page = _page,
                limit = 10,
                requestedMonth = requestedMonth
            )
        }
        if (data != null) {
            val connectedData = _pillListForOneMonth.value.orEmpty() + data.toMutableList()
            _pillListForOneMonth.value =
                connectedData.sortedByDescending { it.id }.toMutableList()
            _page += data.size
        }
        if (!message.isNullOrEmpty()) {
            _message.value = message.toString()
        }
        _onProcess.value = false
    }

    suspend fun savePillToData(pillName: String, time: Date?, id: String) {
        _onProcess.value = true
        val pill = Pill(
            drugName = pillName,
            whenYouTookDate = TimeUtils.toStringCalendar(Date()),
            whenYouTookHour = TimeUtils.toStringClock(Date()),
            month = monthOfToday,
            userMail = currentUserMail
        )
        pillRepository.saveNewPill(pill)
        val maxId = _pillListForOneMonth.value?.maxBy { it.id.toInt() }
        val pillWithId = pill.apply {
            if (maxId != null)
                this.id = (maxId.id.toInt() + 1).toString()
        }


        _pillListForOneMonth.value = _pillListForOneMonth.value?.apply {
            add(pillWithId)
        }
        _allPills.value = _allPills.value?.apply {
            add(pillWithId)
        }
        _pillListForOneMonth.value?.sortByDescending { it.id }
        _allPills.value?.sortByDescending { it.id }
        _onProcess.value = false
    }

    fun deletePill(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pillRepository.deletePill(id, currentUserMail)
        }
    }

    fun checkPermissions(context: Context): Boolean {
        val permissionUtils = PermissionUtils()
        return permissionUtils.hasPermission(context)
    }


    fun lastUsedNameOfPills(): MutableList<String>? {
        return _allPills.value
            ?.distinctBy { it.drugName.lowercase() }
            ?.map { it.drugName }
            ?.toMutableList()
    }

    private fun createColorPallet(): ArrayList<Int> {
        return (ColorTemplate.VORDIPLOM_COLORS + ColorTemplate.JOYFUL_COLORS)
            .toCollection(ArrayList())
    }


    suspend fun prepareChartData(): PieDataSet {
        _onProcess.value = true
        return withContext(Dispatchers.Default) {
            val pillsCountMap = mutableMapOf<String, Float>()

            val tempPillList =
                _allPills.value?.map { it.drugName.capitalize() }?.toMutableList()
                    ?: mutableListOf()

            tempPillList.forEach { drugName ->
                pillsCountMap[drugName] = pillsCountMap.getOrDefault(drugName, 0f) + 1f
            }

            val pieData = pillsCountMap.map { (name, count) -> PieEntry(count, name) }

            val pieDataSet = PieDataSet(pieData, "").apply {
                setColors(createColorPallet())
                valueTextColor = Color.BLACK
                valueTextSize = 18f
            }
            withContext(Dispatchers.Main) {
                _onProcess.value = false
            }

            pieDataSet
        }
    }

    suspend fun prepareChartDataForSpecifiedMonth(): PieDataSet? {
        return withContext(Dispatchers.Default) {
            val pillsCountMap = _pillListForOneMonth.value
                ?.groupBy { it.drugName.capitalize() }
                ?.mapValues { (_, pills) -> pills.size.toFloat() }

            val pieData = if (pillsCountMap.isNullOrEmpty()) {
                arrayListOf(PieEntry(1f, "Bu tarihte hiç ilaç eklemediniz"))
            } else {
                pillsCountMap.map { (name, count) -> PieEntry(count, name) }
            }

            PieDataSet(pieData, "").apply {
                valueTextColor = Color.BLACK
                valueTextSize = 18f
                setColors(createColorPallet())
            }
        }
    }
}