package com.yucox.pillpulse.view

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.yucox.pillpulse.databinding.ActivityAlarmOnBinding
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.util.TimeUtils
import com.yucox.pillpulse.viewmodel.PillViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class AlarmOnActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmOnBinding
    private val viewModel: PillViewModel by viewModels()
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
        val alarmInfo = intent.getSerializableExtra("alarmInfo") as AlarmInfo
        val mpUriString = intent.getSerializableExtra("mp")
        val mpUri: Uri
        val mp: MediaPlayer
        if (mpUriString != null) {
            mpUri = Uri.parse(mpUriString.toString())
            mp = MediaPlayer.create(this.applicationContext, mpUri)
            mp.stop()
        }

        val formattedTime = TimeUtils.toStringClock(alarmInfo.alarmTime)

        binding.showTimeTv.text = formattedTime
        binding.noteTv.text = alarmInfo.pillName + "\n" + alarmInfo.info

        binding.iTokeBtn.setOnClickListener {
            savePill(alarmInfo)
        }
    }

    private fun savePill(alarmInfo: AlarmInfo) {
        alarmInfo.info = "Not almadınız."

        viewModel.savePillLocale(
            alarmInfo.pillName,
            alarmInfo.alarmTime,
            ""
        )
        val intent = Intent(
            this@AlarmOnActivity,
            MainActivity::class.java
        )
        startActivity(intent)
        finish()
    }
}