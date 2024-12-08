package com.yucox.pillpulse.presentation.view.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yucox.pillpulse.presentation.viewmodel.LoginViewModel
import com.yucox.pillpulse.databinding.RegisterActivityBinding
import com.yucox.pillpulse.presentation.effect.LoginEffect
import com.yucox.pillpulse.presentation.event.LoginEvent
import com.yucox.pillpulse.presentation.state.LoginState
import com.yucox.pillpulse.presentation.view.MainActivity
import com.yucox.pillpulse.util.showToastLong
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding : RegisterActivityBinding
    @Inject lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
        observeViewModel()
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

        if (state.isLoggedIn) {
            navigateToMain()
        }
    }

    private fun handleEffect(effect: LoginEffect) {
        when (effect) {
            is LoginEffect.NavigateToMain -> navigateToMain()
            is LoginEffect.ShowError -> showToastLong(effect.message)
            is LoginEffect.NavigateBack -> finish()
            else -> Unit
        }
    }

    private fun setupUI() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.registerBtn.setOnClickListener {
            handleRegister()
        }

        binding.registerToLogin.setOnClickListener {
            viewModel.onEvent(LoginEvent.NavigateBack)
        }

        binding.backToLoginPage.setOnClickListener {
            viewModel.onEvent(LoginEvent.NavigateBack)
        }
    }

    private fun handleRegister() {
        val email = binding.mailEt.text.toString()
        val password = binding.passwordEt.text.toString()

        viewModel.onEvent(
            LoginEvent.Register(
                email = email,
                password = password
            )
        )
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}