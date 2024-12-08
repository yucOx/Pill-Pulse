package com.yucox.pillpulse.presentation.view.alarm

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.yucox.pillpulse.databinding.ActivityAlarmOnBinding
import com.yucox.pillpulse.domain.model.AlarmInfo
import com.yucox.pillpulse.presentation.event.AlarmEvent
import com.yucox.pillpulse.presentation.event.MainEvent
import com.yucox.pillpulse.presentation.view.MainActivity
import com.yucox.pillpulse.presentation.viewmodel.PillViewModel
import com.yucox.pillpulse.util.toFormattedTimeString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmOnActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmOnBinding
    private val viewModel: PillViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding = ActivityAlarmOnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val alarmInfo = intent.getSerializableExtra("alarmInfo") as AlarmInfo
        val mpUriString = intent.getSerializableExtra("mp")
        val mpUri: Uri
        val mp: MediaPlayer
        if (mpUriString != null) {
            mpUri = Uri.parse(mpUriString.toString())
            mp = MediaPlayer.create(this.applicationContext, mpUri)
            mp.stop()
        }

        val formattedTime = alarmInfo.alarmTime.toFormattedTimeString()
        binding.showTimeTv.text = formattedTime
        binding.noteTv.text = alarmInfo.pillName + "\n" + alarmInfo.info

        binding.iTokeBtn.setOnClickListener {
            savePill(alarmInfo)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    private fun savePill(alarmInfo: AlarmInfo) {
        lifecycleScope.launch {
            alarmInfo.info = "Not almadınız."
            viewModel.onEvent(MainEvent.SavePill(alarmInfo.pillName))
            val intent = Intent(
                this@AlarmOnActivity,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }
}