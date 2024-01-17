package com.yucox.pillpulse.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.CreateAlarmActivityBinding
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.model.BroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class CreateAlarm : AppCompatActivity() {
    private lateinit var binding : CreateAlarmActivityBinding
    private lateinit var picker : MaterialTimePicker
    private lateinit var calendar : Calendar
    private lateinit var alarmManager: AlarmManager
    private var auth = FirebaseAuth.getInstance()
    private var database = FirebaseDatabase.getInstance()
    private var ref = database.getReference("Alarms")
    private var key = ref.push().key
    lateinit var mAdView : AdView
    var repeat = 0
    private lateinit var pendingIntent: PendingIntent
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CreateAlarmActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendar = Calendar.getInstance()
        setListeners()
        initBannerAd(binding.adView)
    }

    private fun initBannerAd(adView: AdView) {
        MobileAds.initialize(this) {}
        mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        listenAdEvent()
    }

    private fun listenAdEvent() {
        mAdView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                println(adError)
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                println("yüklendi")
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }

    private fun setListeners() {
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
    }

    private fun repeatCheck() {
        if(repeat == 0){
            repeat = 1
            binding.repeatBtn.setImageResource(R.drawable.repeatfocus)
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView,"Hatırlatıcı her gün tekrar edecek şekilde ayarlandı.",Snackbar.LENGTH_SHORT).show()
        }
        else{
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView,"Hatırlatıcı tek sefer için ayarlandı.",Snackbar.LENGTH_SHORT).show()
            repeat = 0
            binding.repeatBtn.setImageResource(R.drawable.repeatnormal)
        }
    }

    private fun setAlarm() {
        if(binding.pillNameEt.text.toString().isNullOrBlank() == false){
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(rootView,"Hatırlatıcı yarın ve sonraki günler çalacak şekilde ayarlandı",Snackbar.LENGTH_LONG).show()
            }else{
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(rootView,"Hatırlatıcı oluşturuldu", Snackbar.LENGTH_LONG).show()
            }
            val requestCode = System.currentTimeMillis().toInt()
            var ai : AlarmInfo = AlarmInfo(requestCode,binding.pillNameEt.text.toString(),binding.pillNoteEt.text.toString(),repeat,auth.currentUser?.email,key,calendar.time,1)
            ref.child(key.toString()).setValue(ai)
                .addOnCompleteListener {
                    CoroutineScope(Dispatchers.Main).launch {
                        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                        val intent = Intent(applicationContext, BroadcastReceiver::class.java)
                        intent.putExtra("alarmInfo",ai)
                        pendingIntent = PendingIntent.getBroadcast(applicationContext,requestCode,intent,PendingIntent.FLAG_IMMUTABLE)
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.timeInMillis,AlarmManager.INTERVAL_DAY,pendingIntent)
                        finish()
                    }
                }
        } else {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView,"İlaç ismini giriniz",Snackbar.LENGTH_SHORT).show()
        }

    }

    private fun showTimePicker() {
        picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(calendar.get(Calendar.HOUR)).setMinute(calendar.get(Calendar.MINUTE)).setTitleText("İlacı içmeniz gerek saati giriniz.")
            .build()
        picker.show(supportFragmentManager,"test")
        picker.addOnPositiveButtonClickListener {
            calendar[Calendar.HOUR_OF_DAY] = picker.hour
            calendar[Calendar.MINUTE] = picker.minute
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(rootView,calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + calendar.get(Calendar.MINUTE) + " Seçildi",Snackbar.LENGTH_LONG).show()
        }
    }
}