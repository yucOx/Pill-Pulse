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
import com.yucox.pillpulse.model.PillRealm
import com.yucox.pillpulse.model.PillTime
import com.yucox.pillpulse.utils.PermissionUtils
import com.yucox.pillpulse.repository.FirebaseUserRepository
import com.yucox.pillpulse.model.UserInfo
import com.yucox.pillpulse.repository.FirebasePillRepository
import com.yucox.pillpulse.repository.LocalePillRepository
import com.yucox.pillpulse.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PillViewModel @Inject constructor(
    private val _firebaseUserRepository: FirebaseUserRepository,
    private val _localePillRepository: LocalePillRepository,
    private val _firebasePillRepository: FirebasePillRepository
) : ViewModel() {
    private val _user = MutableLiveData<UserInfo>()
    private val _specifiedMonthPills = MutableLiveData<MutableList<PillRealm>>()
    private val _message = MutableLiveData<String>()

    val user: LiveData<UserInfo> get() = _user
    var rmPillList: LiveData<List<PillRealm>> = _localePillRepository.fetchPills()
    val specifiedMonthPills: LiveData<MutableList<PillRealm>> get() = _specifiedMonthPills
    val message = MutableLiveData<String>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            fetchUserInfo()
        }
    }

    fun synchronizePills() {
        viewModelScope.launch {
            val idList = getLocaleIdList(rmPillList.value?.toMutableList())
            var result: List<PillTime>? = null
            val localConvertedPills = mutableListOf<PillTime>()
            rmPillList.value?.forEach {
                val convertedPill = convertToPillTime(it)
                localConvertedPills.add(convertedPill)
            }
            try {
                result = withContext(Dispatchers.IO) {
                    idList?.let {
                        _firebasePillRepository.synchronizeData(
                            localeIdList = it,
                            localConvertedPills = localConvertedPills
                        )
                    }
                }
            } catch (e: Exception) {
                _message.value = e.localizedMessage
            }
            result?.let { dataPills ->
                dataPills.forEach { dataPill ->
                    savePillLocale(
                        dataPill.drugName,
                        dataPill.whenYouTook,
                        dataPill.key
                    )
                }
            }
        }
    }

    private fun convertToLocalePillObject(
        pill: PillTime
    ): PillRealm? {
        var convertedPill: PillRealm? = null
        convertedPill = (PillRealm().apply {
            this.drugName = pill.drugName
            this.id = ObjectId()
            this.tokeDate = TimeUtils.toStringCalendar(pill.whenYouTook)
            this.tokeTime = TimeUtils.toStringClock(pill.whenYouTook)
        })
        return convertedPill
    }

    private fun convertToPillTime(
        pill: PillRealm
    ): PillTime {
        val calendar = Calendar.getInstance()
        val time = TimeUtils.toDateClock(pill.tokeTime)
        val date = TimeUtils.toDateCalendar(pill.tokeDate)
        calendar.set(Calendar.HOUR_OF_DAY, time.hours)
        calendar.set(Calendar.MINUTE, time.minutes)
        calendar.set(Calendar.DATE, date.date)

        val convertedPill = PillTime(
            drugName = pill.drugName,
            whenYouTook = calendar.time,
            key = pill.id.toHexString(),
            userMail = _user.value?.mail.toString()
        )
        return convertedPill
    }

    private fun getLocaleIdList(
        localePillList: MutableList<PillRealm>?
    ): List<String>? {
        if (localePillList == null)
            return null

        return localePillList.map { it.id.toHexString() }
    }

    fun savePillLocale(pillName: String, time: Date?, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val pill = PillRealm().apply {
                this.drugName = pillName
                if (time != null) {
                    this.tokeTime = TimeUtils.toStringClock(time)
                    this.tokeDate = TimeUtils.toStringCalendar(time)
                } else {
                    this.tokeTime = TimeUtils.toStringClock(Date())
                    this.tokeDate = TimeUtils.toStringCalendar(Date())
                }
                if (id.isNotEmpty())
                    this.id = ObjectId(id)
                _user.value?.mail?.let {
                    this.userMail = it
                }
            }
            _localePillRepository.savePill(pill)
        }
    }

    fun deletePillLocale(id: ObjectId) {
        viewModelScope.launch {
            _localePillRepository.removePill(id)
        }
    }

    fun checkPermissions(context: Context): Boolean {
        val permissionUtils = PermissionUtils()
        return permissionUtils.hasPermission(context)
    }

    fun fetchUserInfo() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                _firebaseUserRepository.fetchMainUserInfo()
            }
            _user.value = user
        }
    }

    fun signOut() {
        _firebaseUserRepository.signOut()
    }

    fun listAllYear() {
        viewModelScope.launch {
            val tempPillList = withContext(Dispatchers.IO) {
                rmPillList.value?.toMutableList()
            }
            tempPillList?.let {
                _specifiedMonthPills.value = it
            }
        }
    }

    fun listPillsByMonth(currentMonth: Int) {
        viewModelScope.launch {
            val tempList = ArrayList<PillRealm>()
            withContext(Dispatchers.IO) {
                rmPillList.value?.let { pills ->
                    for (pill in pills) {
                        val month = TimeUtils.toDateCalendar(pill.tokeDate)
                        if (month.month == currentMonth) {
                            tempList.add(pill)
                        }
                    }
                }
                if (tempList.isEmpty()) {
                    tempList.add(
                        PillRealm().apply {
                            this.drugName = "Bu tarihte ilaç eklemediniz."
                        }
                    )
                }
            }
            _specifiedMonthPills.value = tempList
        }
    }

    fun listPillsByOrderedName(): ArrayList<String> {
        val orderedList = ArrayList<String>()
        val tempPillList = rmPillList.value
        if (!tempPillList.isNullOrEmpty()) {
            tempPillList.forEach {
                println(it.drugName)
                val capitalizedOne = it.drugName.capitalize()
                if (capitalizedOne !in orderedList)
                    orderedList.add(capitalizedOne)
            }
        }
        return orderedList
    }

    private fun createColorPallet(): ArrayList<Int> {
        val colorsArray = ArrayList<Int>()
        for (a in ColorTemplate.VORDIPLOM_COLORS) {
            colorsArray.add(a)
        }
        for (a in ColorTemplate.JOYFUL_COLORS) {
            colorsArray.add(a)
        }
        return colorsArray
    }

    fun prepareChartData(): PieDataSet {
        val pillsCountMap = HashMap<String, Float>()
        var tempPillList = rmPillList.value?.toMutableList()

        if (tempPillList == null)
            tempPillList = mutableListOf<PillRealm>()

        for (pill in tempPillList) {
            if (!pillsCountMap.containsKey(pill.drugName.capitalize())) {
                pillsCountMap.put(pill.drugName.capitalize(), 0f)
            }
        }
        for (pill in tempPillList) {
            val capitalizedOne = pill.drugName.capitalize()
            if (pillsCountMap.containsKey(capitalizedOne)) {
                pillsCountMap[capitalizedOne] =
                    pillsCountMap[capitalizedOne]!! + 1f
            } else {
                pillsCountMap[capitalizedOne] = 1f
            }
        }
        val pieData = ArrayList<PieEntry>().let {
            pillsCountMap.forEach { name, count ->
                it.add(PieEntry(count, name))
            }
            it
        }
        val pieDataSet = PieDataSet(pieData, "")
        pieDataSet.setColors(createColorPallet())
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 18f

        return pieDataSet
    }

    fun prepareChartDataForSpecifiedMonth(): PieDataSet? {
        val pillsCountMap = HashMap<String, Float>()
        var tempSpecifiedPills = _specifiedMonthPills.value
        if (tempSpecifiedPills == null)
            tempSpecifiedPills = mutableListOf<PillRealm>()

        for (pill in tempSpecifiedPills) {
            if (!pillsCountMap.containsKey(pill.drugName.capitalize())) {
                pillsCountMap.put(pill.drugName.capitalize(), 0f)
            }
        }
        for (pill in tempSpecifiedPills) {
            val capitalizedOne = pill.drugName.capitalize()
            if (pillsCountMap.containsKey(capitalizedOne)) {
                pillsCountMap[capitalizedOne] =
                    pillsCountMap[capitalizedOne]!! + 1f
            } else {
                pillsCountMap[capitalizedOne] = 1f
            }
        }
        val pieData = ArrayList<PieEntry>().let {
            pillsCountMap.forEach { name, count ->
                it.add(PieEntry(count, name))
            }
            it
        }

        val pieDataSet = PieDataSet(pieData, "")
        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 18f
        pieDataSet.setColors(createColorPallet())
        return pieDataSet
    }

}