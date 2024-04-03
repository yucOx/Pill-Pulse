package com.yucox.pillpulse.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.yucox.pillpulse.ViewModel.LoginViewModel
import com.yucox.pillpulse.databinding.RegisterActivityBinding
import com.yucox.pillpulse.Model.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: RegisterActivityBinding
    private lateinit var viewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        viewModel.status.observe(this) { isSuccessful ->
            if (isSuccessful == 1) {
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.saveUserInfo(viewModel).await()
                    Toast.makeText(
                        this@RegisterActivity,
                        "Hesap oluşturuldu, giriş yapabilirsin",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.error.observe(this) {
            if (it != null) {
                Toast.makeText(
                    this,
                    "$it",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.registerBtn.setOnClickListener {
            val name = binding.nameEt.text.toString()
            val surname = binding.surnameEt.text.toString()
            val mail = binding.mailEt.text.toString()
            val password = binding.passwordEt.text.toString()

            if (name.isBlank() ||
                surname.isBlank() ||
                mail.isBlank() ||
                password.isBlank()
            ) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Boş alanları doldurun",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            viewModel.updateUser(UserInfo(name, surname, mail), password)
            viewModel.createNewAccount(viewModel)
        }

        binding.registerToLogin.setOnClickListener {
            finish()
        }
    }
}