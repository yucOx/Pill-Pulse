package com.yucox.pillpulse.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.AddTimeActivityBinding
import com.yucox.pillpulse.model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class AddTimeActivity : AppCompatActivity() {
    private lateinit var binding: AddTimeActivityBinding
    private var database = FirebaseDatabase.getInstance()
    private var auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddTimeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListenerForInputs()
        save()

        backToPrevious()
    }

    private fun save() {
        binding.saveBtn.setOnClickListener {
            saveData(
                binding.pillNameText.text.toString(),
                binding.noteEt.text.toString(),
                database,
                auth.currentUser?.email.toString()
            )
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveData(
        pillName: String,
        note: String,
        database: FirebaseDatabase,
        userMail: String
    ) {
        var getRealTime = Date()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        var formatedDate = dateFormat.format(getRealTime)

        var key = database.getReference("Pills").push().key.toString()

        if (!pillName.isNullOrBlank()) {
            if (note.isBlank()) {
                var pillTimeAndNote =
                    PillTime(pillName, "Not almadınız.", getRealTime, userMail, key)
                database.getReference("Pills").child(key).setValue(pillTimeAndNote)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            CoroutineScope(Dispatchers.Main).launch {
                                finish()
                            }
                        }
                    }
            } else {
                var pillTimeAndNote = PillTime(pillName, note, getRealTime, userMail, key)
                database.getReference("Pills").child(key).setValue(pillTimeAndNote)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            CoroutineScope(Dispatchers.Main).launch {
                                finish()
                            }
                        }
                    }
            }
        }
    }

    private fun setListenerForInputs() {
        binding.pillNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.pillNameText.setBackgroundResource(R.drawable.pillinputfocussed)
            }

            override fun afterTextChanged(p0: android.text.Editable?) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding.pillNameText.setBackgroundResource(R.drawable.pillinputafter)
                    if (p0.isNullOrBlank()) {
                        binding.pillNameText.setBackgroundResource(R.drawable.pillinputnormal)
                    }
                }
            }

        })
        binding.noteEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.noteEt.setBackgroundResource(R.drawable.noteinputfocussed)
            }

            override fun afterTextChanged(p0: Editable?) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding.noteEt.setBackgroundResource(R.drawable.noteinputafter)
                    if (p0.isNullOrBlank()) {
                        binding.noteEt.setBackgroundResource(R.drawable.noteinputnormal)
                    }
                }
            }

        })
    }
    private fun backToPrevious() {
        binding.backIv.setOnClickListener {
            finish()
        }
    }
}