package com.yucox.pillpulse.vimodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucox.pillpulse.model.PillTime
import com.yucox.pillpulse.model.UserInfo
import com.yucox.pillpulse.repository.FirebasePillRepository
import com.yucox.pillpulse.repository.FirebaseUserRepository
import com.yucox.pillpulse.utils.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val _firebaseUserRepository: FirebaseUserRepository,
    private val _pillRepository: FirebasePillRepository
) : ViewModel() {
    private val _user = MutableLiveData<UserInfo>()
    private val _pillList = MutableLiveData<ArrayList<PillTime>>()
    private val _specifiedMonthPills = MutableLiveData<ArrayList<PillTime>>()
    private val _saveCheck = MutableLiveData<Boolean>()

    val user: LiveData<UserInfo> = _user
    val pillList: LiveData<ArrayList<PillTime>> = _pillList
    val specifiedMonthPills: LiveData<ArrayList<PillTime>> = _specifiedMonthPills
    val saveCheck: LiveData<Boolean> get() = _saveCheck


    fun checkPermissions(context: Context): Boolean {
        val permissionUtils = PermissionUtils()

        return permissionUtils.hasPermission(context)
    }

    fun updatePillList(pillsInfo: ArrayList<PillTime>) {
        _pillList.value = pillsInfo
    }

    fun fetchUserInfo() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                _firebaseUserRepository.fetchMainUserInfo()
            }
            _user.value = user
        }
    }

    fun fetchPills() {
        viewModelScope.launch {
            val pills = withContext(Dispatchers.IO) {
                _pillRepository.fetchPillsInfo()
            }
            updatePillList(pills)
        }
    }

    fun signOut() {
        _firebaseUserRepository.signOut()
    }

    fun saveNewPill(pill: String, note: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                _pillRepository.saveNewPill(pill, note)
            }
            _saveCheck.value = result
        }
    }

    fun listAllYear() {
        _specifiedMonthPills.value = _pillList.value
    }

    fun listSpecifiedMonth(currentMonth: Int) {
        val tempList = ArrayList<PillTime>()
        _pillList.value?.let { pills ->
            for (pill in pills) {
                if (pill.whenYouTook.month == currentMonth) {
                    tempList?.add(pill)
                }
            }
        }
        if (tempList.isEmpty()) {
            tempList.add(
                PillTime(
                    "Bu tarihte ilaç eklemediniz."
                )
            )
        }
        _specifiedMonthPills.value = tempList
    }

    fun listPillsByOrderedName(): ArrayList<String> {
        val orderedList = ArrayList<String>()
        _pillList.value?.let {
            for (a in it) {
                val capitalizedOne = a.drugName.capitalize()
                if (capitalizedOne !in orderedList)
                    orderedList.add(capitalizedOne)
            }
        }
        return orderedList
    }

    fun prepareChartData(): HashMap<String, Float> {
        val drugCount = HashMap<String, Float>()

        _pillList.value?.let {
            if (it.isNotEmpty()) {
                for (a in it) {
                    if (!drugCount.containsKey(a.drugName.capitalize())) {
                        drugCount.put(a.drugName.capitalize(), 0f)
                    }
                }
                for (a in it) {
                    val capitalizedOne = a.drugName.capitalize()
                    if (drugCount.containsKey(capitalizedOne)) {
                        drugCount[capitalizedOne] =
                            drugCount[capitalizedOne]!! + 1f
                    } else {
                        drugCount[capitalizedOne] = 1f
                    }
                }
            }
        }
        return drugCount
    }

    fun prepareChartDataForSpecifiedMonth(): HashMap<String, Float> {
        val drugCount = HashMap<String, Float>()

        _specifiedMonthPills.value?.let {
            if (it.isNotEmpty()) {
                for (a in it) {
                    if (!drugCount.containsKey(a.drugName.capitalize())) {
                        drugCount.put(a.drugName.capitalize(), 0f)
                    }
                }
                for (a in it) {
                    val capitalizedOne = a.drugName.capitalize()
                    if (drugCount.containsKey(capitalizedOne)) {
                        drugCount[capitalizedOne] =
                            drugCount[capitalizedOne]!! + 1f
                    } else {
                        drugCount[capitalizedOne] = 1f
                    }
                }
            }
        }
        return drugCount
    }

}