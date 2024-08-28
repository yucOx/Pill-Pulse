package com.yucox.pillpulse.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yucox.pillpulse.viewmodel.LoginViewModel
import com.yucox.pillpulse.databinding.RegisterActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: RegisterActivityBinding
    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.message.observe(this) {
            if (it == null) {
                return@observe
            }
            showToast(it)
        }
    }

    private fun setupUI() {
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.registerBtn.setOnClickListener {
            lifecycleScope.launch {
                val mail = binding.mailEt.text.toString()
                val password = binding.passwordEt.text.toString()
                if (mail.isBlank() || password.isBlank()) {
                    showToast("Boş alanları doldurun")
                    return@launch
                }
                viewModel.createNewAccount(mail, password)
            }
        }

        binding.registerToLogin.setOnClickListener {
            finish()
        }

        binding.backToLoginPage.setOnClickListener {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this@RegisterActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}