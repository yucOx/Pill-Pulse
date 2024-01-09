package com.yucox.pillpulse.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.databinding.RegisterActivityBinding
import com.yucox.pillpulse.model.UserInfo

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: RegisterActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var auth = FirebaseAuth.getInstance()
        var database = FirebaseDatabase.getInstance()
        binding.progressBar.visibility = View.GONE

        binding.registerButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            registerToApp(
                binding.registerMail.text.toString(),
                binding.registerPass.text.toString(),
                binding.registerName.text.toString(),
                binding.registerSurname.text.toString(),
                auth,
                database,
                binding.progressBar
            )
        }
    }

    private fun registerToApp(
        mail: String,
        pass: String,
        name: String,
        surname: String,
        auth: FirebaseAuth,
        database: FirebaseDatabase,
        progressBar: ProgressBar
    ) {
        if(pass.length < 8){
            Toast.makeText(this,"Şifreniz en az 8 haneden oluşmalıdır..",Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }
        if (mail.isNullOrEmpty() == false && pass.isNullOrEmpty() == false && name.isNullOrEmpty() == false && surname.isNullOrEmpty() == false)
            auth.createUserWithEmailAndPassword(mail,pass)
                .addOnSuccessListener {
                    var userInfo = UserInfo(name,surname,mail)
                    database.getReference("UserInfo").push().setValue(userInfo)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this,"Kayıt başarılı, giriş sayfasına yönlendiriliyorsunuz.",Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this,"Mail daha önceden kaydedilmiş.",Toast.LENGTH_SHORT).show()
                }
        else {
            progressBar.visibility = View.GONE
            Toast.makeText(this@RegisterActivity,"Lütfen bütün boş alanları doldurun.",Toast.LENGTH_SHORT)
        }
    }
}