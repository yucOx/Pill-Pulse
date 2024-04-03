package com.yucox.pillpulse.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.R
import com.yucox.pillpulse.ViewModel.AlarmViewModel
import com.yucox.pillpulse.databinding.CreateAlarmActivityBinding
import com.yucox.pillpulse.Model.AlarmInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateAlarm : AppCompatActivity() {
    private lateinit var binding: CreateAlarmActivityBinding
    private lateinit var viewModel: AlarmViewModel

    private lateinit var calendar: Calendar
    private var auth = FirebaseAuth.getInstance()
    private lateinit var mAdView: AdView
    private var repeat = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateAlarmActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendar = Calendar.getInstance()

        viewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)

        binding.setAlarmBtn.setOnClickListener {
            setAlarm()
        }

        binding.selectTimeBtn.setOnClickListener {
            showTimePicker()
        }

        binding.repeatBtn.setOnClickListener {
            repeatCheck()
        }

        binding.backIv.setOnClickListener {
            finish()
        }

        initBannerAd(binding.adView)
    }


    private fun repeatCheck() {
        val rootView = findViewById<View>(android.R.id.content)
        if (repeat == 0) {
            repeat = 1
            binding.repeatBtn.setImageResource(R.drawable.repeat_focus3)
            Snackbar.make(
                rootView,
                "Hatırlatıcı her gün tekrar edecek şekilde ayarlandı.",
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            Snackbar.make(
                rootView,
                "Hatırlatıcı tek sefer için ayarlandı.",
                Snackbar.LENGTH_SHORT
            ).show()

            repeat = 0
            binding.repeatBtn.setImageResource(R.drawable.repeat_normal3)
        }
    }

    private fun setAlarm() {
        val key = FirebaseDatabase.getInstance()
            .getReference("Alarms")
            .push().key

        val rootView = findViewById<View>(android.R.id.content)
        if (!binding.pillNameEt.text.toString().isBlank()) {
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val requestCode = System.currentTimeMillis().toInt()
            val alarm = AlarmInfo(
                requestCode,
                binding.pillNameEt.text.toString(),
                binding.pillNoteEt.text.toString(),
                repeat,
                auth.currentUser?.email,
                key,
                calendar.time,
                1
            )

            CoroutineScope(Dispatchers.Main).launch {
                viewModel.updateAlarm(alarm)

                viewModel.setAlarm(
                    this@CreateAlarm,
                    calendar
                )

                val result = viewModel.savePillAlarm(
                    key!!,
                    viewModel
                )

                if (result) {
                    Snackbar.make(
                        rootView,
                        "Hatırlatıcı ayarlandı",
                        Snackbar.LENGTH_LONG
                    ).show()
                    delay(2000)
                    finish()
                }
            }


        } else {
            Snackbar.make(
                rootView,
                "İlaç ismini giriniz",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText("İlacı içmeniz gerek saati giriniz.")
            .build()
        picker.show(supportFragmentManager, "test")
        picker.addOnPositiveButtonClickListener {
            calendar[Calendar.HOUR_OF_DAY] = picker.hour
            calendar[Calendar.MINUTE] = picker.minute
            val sdf = SimpleDateFormat("hh:mm", Locale.getDefault())
            val formattedTime = sdf.format(calendar.time)
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(
                rootView,
                "$formattedTime Seçildi",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun initBannerAd(adView: AdView) {
        MobileAds.initialize(this) {}
        mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}