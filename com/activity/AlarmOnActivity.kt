package com.yucox.pillpulse.activity

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.ActivityAlarmOnBinding
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmOnActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmOnBinding
    var database = FirebaseDatabase.getInstance()
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
        var alarmInfo = intent.getSerializableExtra("alarmInfo") as AlarmInfo
        var mpUriString = intent.getSerializableExtra("mp")
        var mpUri : Uri
        var mp : MediaPlayer
        if(mpUriString != null){
            mpUri = Uri.parse(mpUriString.toString())
            mp = MediaPlayer.create(this.applicationContext,mpUri)
            mp.stop()
        }

        var calendar = Calendar.getInstance()
        calendar.time = alarmInfo.alarmTime
        binding.showTimeTv.text =
            calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + calendar.get(Calendar.MINUTE)
                .toString()
        binding.noteTv.text = alarmInfo.pillName + "\n" + alarmInfo.info
        var calendar2 = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH,calendar2.get(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.MONTH,calendar2.get(Calendar.MONTH))

        binding.iTokeBtn.setOnClickListener {
            saveToData(alarmInfo)
        }
    }

    private fun saveToData(alarmInfo: AlarmInfo) {
        var ref = database.getReference("Pills").child(alarmInfo.alarmLocation.toString())
        var pillTime = PillTime(
            alarmInfo.pillName,
            alarmInfo.info,
            alarmInfo.alarmTime,
            alarmInfo.userMail.toString(),
            alarmInfo.alarmLocation.toString()
        )
        ref.setValue(pillTime)
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val intent = Intent(this@AlarmOnActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
    }

}