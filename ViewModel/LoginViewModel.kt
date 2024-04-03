package com.yucox.pillpulse.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.yucox.pillpulse.Repository.UserRepository
import com.yucox.pillpulse.Model.UserInfo

class LoginViewModel : ViewModel() {
    private val _user = MutableLiveData<UserInfo>()
    private val _pass = MutableLiveData<String>()
    private val _status = MutableLiveData<Int>()
    private val _error = MutableLiveData<String>()

    val user: LiveData<UserInfo> = _user
    val pass: LiveData<String> = _pass
    val status: LiveData<Int> = _status
    val error: LiveData<String> = _error

    private val _repository = UserRepository()

    fun isAnyoneIn() : Int{
        return _repository.isAnyoneIn()
    }
    fun updateErrorMessage(errorMessage: String) {
        _error.value = errorMessage
    }

    fun updateUser(newUser: UserInfo, userPass: String) {
        _user.value = newUser
        _pass.value = userPass
    }

    fun createNewAccount(viewModel: LoginViewModel) {
        _repository.createAccount(viewModel)
    }

    fun updateStatus(newStat: Int) {
        _status.value = newStat
    }

    fun saveUserInfo(viewModel: LoginViewModel): Task<Boolean> {
        return _repository.saveUserInfo(viewModel)
    }

    fun logIn(viewModel : LoginViewModel){
        _repository.takeMe(viewModel)
    }

}