package com.yucox.pillpulse.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yucox.pillpulse.domain.repository.IFirebaseUserRepository
import com.yucox.pillpulse.presentation.effect.LoginEffect
import com.yucox.pillpulse.presentation.event.LoginEvent
import com.yucox.pillpulse.presentation.state.LoginState
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val authRepository: IFirebaseUserRepository
) : ViewModel() {
    private val _state = MutableLiveData(LoginState())
    val state: LiveData<LoginState> = _state

    private val _effect = MutableLiveData<LoginEffect>()
    val effect: LiveData<LoginEffect> = _effect

    init {
        checkLoggedInStatus()
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Login -> login(event.email, event.password)
            is LoginEvent.Register -> register(event.email, event.password)
            is LoginEvent.CheckLoggedIn -> checkLoggedInStatus()
            is LoginEvent.NavigateToRegister -> navigateToRegister()
            is LoginEvent.NavigateBack -> _effect.postValue(LoginEffect.NavigateBack)
        }
    }

    private fun login(email: String, password: String) {
        if (!validateInput(email, password)) return

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            try {
                authRepository.login(email, password)
                    .onSuccess {
                        updateState { it.copy(isLoggedIn = true) }
                        _effect.postValue(LoginEffect.NavigateToMain)
                    }
                    .onFailure { error ->
                        handleError(error)
                    }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    private fun register(email: String, password: String) {
        if (!validateInput(email, password)) return

        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            try {
                authRepository.register(email, password)
                    .onSuccess {
                        updateState { it.copy(isLoggedIn = true) }
                        _effect.postValue(LoginEffect.NavigateToMain)
                    }
                    .onFailure { error ->
                        handleError(error)
                    }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _effect.value = LoginEffect.ShowError("Boş alanları doldurun")
            return false
        }
        return true
    }

    private fun handleError(error: Throwable) {
        _effect.postValue(
            LoginEffect.ShowError(
                error.message ?: "Beklenmedik bir hata"
            )
        )
    }

    private fun checkLoggedInStatus() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                _effect.value = LoginEffect.NavigateToMain
            }
        }
    }

    private fun navigateToRegister() {
        _effect.value = LoginEffect.NavigateToRegister
    }

    private fun updateState(update: (LoginState) -> LoginState) {
        _state.value = update(_state.value ?: LoginState())
    }
}