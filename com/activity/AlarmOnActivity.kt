package com.yucox.pillpulse.activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.yucox.pillpulse.databinding.ActivityAlarmOnBinding
import java.util.Calendar

class AlarmOnActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAlarmOnBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmOnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateIntentData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        updateIntentData()
    }

    private fun updateIntentData() {
        var note = intent.getStringExtra("note2")
        var time = intent.getStringExtra("time2")
        binding.showTimeTv.text = time
        binding.noteTv.text = note.toString()
    }

}