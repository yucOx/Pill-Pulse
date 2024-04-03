package com.yucox.pillpulse.ViewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.yucox.pillpulse.PermissionUtils
import com.yucox.pillpulse.Repository.PillRepository
import com.yucox.pillpulse.Repository.UserRepository
import com.yucox.pillpulse.Model.PillTime
import com.yucox.pillpulse.Model.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    private val _user = MutableLiveData<UserInfo>()
    private val _pillList = MutableLiveData<ArrayList<PillTime>>()
    private val _specifiedMonthPills = MutableLiveData<ArrayList<PillTime>>()

    val user: LiveData<UserInfo> = _user
    val pillList: LiveData<ArrayList<PillTime>> = _pillList
    val specifiedMonthPills: LiveData<ArrayList<PillTime>> = _specifiedMonthPills

    private val userRepository = UserRepository()
    private val pillRepository = PillRepository()

    fun checkPermissions(context: Context): Boolean {
        val permissionUtils = PermissionUtils()

        return permissionUtils.hasPermission(context)
    }

    fun updateUser(newUser: UserInfo) {
        _user.value = newUser
    }

    fun updatePillList(pillsInfo: ArrayList<PillTime>) {
        _pillList.value = pillsInfo
    }

    fun fetchUserInfo(viewModel: MainViewModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = userRepository.fetchMainUserInfo(viewModel)
            withContext(Dispatchers.Main) {
                updateUser(fetchedData)
            }
        }
    }

    fun fetchPills(viewModel: MainViewModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedData = pillRepository.fetchPillsInfo(viewModel)
            withContext(Dispatchers.Main) {
                updatePillList(fetchedData)
            }
        }
    }

    fun signOut() {
        userRepository.signOut()
    }

    fun saveNewPill(pill: String, note: String): Task<Boolean> {
        return pillRepository.saveNewPill(pill, note)
    }

    fun listAllYear(){
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