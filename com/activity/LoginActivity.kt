package com.yucox.pillpulse.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.yucox.pillpulse.databinding.LoginActivityBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : LoginActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBarLogin.visibility = View.GONE

        var auth = FirebaseAuth.getInstance()

        isUserIn(auth)
        binding.loginButton.setOnClickListener {
            binding.progressBarLogin.visibility = View.VISIBLE
            login(binding.signinMail.text.toString(),binding.password.text.toString(),auth)
        }
        binding.letmetoRegister.setOnClickListener {
            toRegisterPage(Intent(this@LoginActivity,RegisterActivity::class.java))
        }
    }

    private fun isUserIn(auth: FirebaseAuth) {
        if(auth.currentUser != null){
            val intent = Intent(this@LoginActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun toRegisterPage(intent: Intent) {
        startActivity(intent)
    }

    private fun login(signinMail: String, password: String, auth: FirebaseAuth) {
        if(signinMail.isNullOrEmpty() == false && password.isNullOrEmpty() == false)
            auth.signInWithEmailAndPassword(signinMail,password)
                .addOnSuccessListener {
                    binding.progressBarLogin.visibility = View.GONE
                    Toast.makeText(this@LoginActivity,"Giriş başarılı",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    binding.progressBarLogin.visibility = View.GONE
                    Toast.makeText(this@LoginActivity,"Şifre veya mail yanlış",Toast.LENGTH_SHORT).show()
                }
        else
        {
            binding.progressBarLogin.visibility = View.GONE
            Toast.makeText(this@LoginActivity,"Boş alan olmamalıdır.",Toast.LENGTH_SHORT).show()
        }
    }
}