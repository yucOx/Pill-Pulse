package com.yucox.pillpulse.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yucox.pillpulse.repository.FirebaseUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val _repository: FirebaseUserRepository) :
    ViewModel() {
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun isAnyoneIn(): Int {
        return _repository.isAnyoneIn()
    }

    private fun updateErrorMessage(errorMessage: String) {
        _message.value = errorMessage
    }

    suspend fun createNewAccount(mail: String, password: String): Boolean {
        val (result, exception) = withContext(Dispatchers.IO) {
            _repository.createAccount(mail, password)
        }
        if (result) {
            return true
        } else {
            updateErrorMessage("Beklenmedik bir hata ile karşılaşıldı")
            return false
        }
    }

    suspend fun logIn(
        mail: String,
        password: String
    ): Boolean {
        val (result, errorMessage) = withContext(Dispatchers.IO) {
            _repository.logInCheck(mail, password)
        }
        if (!result) {
            updateErrorMessage("Beklenmedik bir hata ile karşılaşıldı")
        }
        return result
    }

    fun logOut() {
        _repository.signOut()
    }
}