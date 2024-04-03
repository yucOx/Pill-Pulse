package com.yucox.pillpulse.View

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yucox.pillpulse.Repository.PillRepository
import com.yucox.pillpulse.databinding.ActivityAlarmOnBinding
import com.yucox.pillpulse.Model.AlarmInfo
import com.yucox.pillpulse.Model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmOnActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmOnBinding
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

        val sdf = SimpleDateFormat("hh:mm", Locale.getDefault())
        val formattedTime = sdf.format(alarmInfo.alarmTime)

        binding.showTimeTv.text = formattedTime.toString()
        binding.noteTv.text = alarmInfo.pillName + "\n" + alarmInfo.info

        binding.iTokeBtn.setOnClickListener {
            savePill(alarmInfo)
        }
    }

    private fun savePill(alarmInfo: AlarmInfo) {
        CoroutineScope(Dispatchers.Main).launch {
            alarmInfo.info = "Not almadınız."
            val pill = PillTime(
                alarmInfo.pillName,
                alarmInfo.info,
                alarmInfo.alarmTime,
                alarmInfo.userMail.toString(),
                alarmInfo.alarmLocation.toString()
            )
            val result = PillRepository().savePillWithSpecifiedTime(
                pill,
                alarmInfo.alarmLocation.toString()
            ).await()

            if (result) {
                val intent = Intent(
                    this@AlarmOnActivity,
                    MainActivity::class.java
                )
                startActivity(intent)
                finish()
            } else {
                return@launch
            }
        }
    }
}