package com.yucox.pillpulse.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.yucox.pillpulse.viewmodel.LoginViewModel
import com.yucox.pillpulse.databinding.LoginActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
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
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isAnyoneIn() == 1) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBtn.setOnClickListener {
            lifecycleScope.launch {
                login()
            }
        }

        binding.letmetoRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private suspend fun login() {
        val mail: String = binding.mailEt.text.toString()
        val pass: String = binding.passwordEt.text.toString()
        if (mail.isBlank() || pass.isBlank()) {
            showToast("Boş alanları doldurun")
            return
        }
        val result = viewModel.logIn(mail, pass)
        if (!result)
            return

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this@LoginActivity,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}