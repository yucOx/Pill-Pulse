package com.yucox.pillpulse.presentation.view.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.yucox.pillpulse.presentation.viewmodel.LoginViewModel
import com.yucox.pillpulse.databinding.LoginActivityBinding
import com.yucox.pillpulse.presentation.effect.LoginEffect
import com.yucox.pillpulse.presentation.event.LoginEvent
import com.yucox.pillpulse.presentation.state.LoginState
import com.yucox.pillpulse.presentation.view.MainActivity
import com.yucox.pillpulse.util.showToastLong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    @Inject
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeViewModel()
        setupUI()
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            handleState(state)
        }

        viewModel.effect.observe(this) { effect ->
            handleEffect(effect)
        }
    }

    private fun handleState(state: LoginState) {
        binding.progressBar.isVisible = state.isLoading
    }

    private fun handleEffect(effect: LoginEffect) {
        when (effect) {
            is LoginEffect.NavigateToMain -> navigateToMain()
            is LoginEffect.NavigateToRegister -> navigateToRegister()
            is LoginEffect.ShowError -> showToastLong(effect.message)
            is LoginEffect.NavigateBack -> finish()
        }
    }

    private fun setupUI() {
        binding.loginBtn.setOnClickListener {
            viewModel.onEvent(
                LoginEvent.Login(
                    email = binding.mailEt.text.toString(),
                    password = binding.passwordEt.text.toString()
                )
            )
        }

        binding.letmetoRegister.setOnClickListener {
            viewModel.onEvent(LoginEvent.NavigateToRegister)
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}