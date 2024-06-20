package com.yucox.pillpulse.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import com.yucox.pillpulse.viewmodel.LoginViewModel
import com.yucox.pillpulse.databinding.LoginActivityBinding
import com.yucox.pillpulse.model.UserInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (viewModel.isAnyoneIn() == 1) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.status.observe(this) { isSuccessful ->
            if (isSuccessful == 1) {
                Toast.makeText(
                    this,
                    "Giriş başarılı, hoş geldin!",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        viewModel.error.observe(this) {
            if (it != null) {
                Toast.makeText(
                    this,
                    "$it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.loginBtn.setOnClickListener {
            val mail: String = binding.mailEt.text.toString()
            val pass: String = binding.passwordEt.text.toString()

            if (mail.isBlank() ||
                pass.isBlank()
            ) {
                Toast.makeText(
                    this@LoginActivity,
                    "Boş alanları doldurun",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            viewModel.updateUser(UserInfo("", "", mail), pass)
            viewModel.logIn()
        }

        binding.letmetoRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.viewModelScope.cancel()
    }
}