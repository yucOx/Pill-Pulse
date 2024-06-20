package com.yucox.pillpulse.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.pillpulse.R
import com.yucox.pillpulse.adapter.AlarmAdapter
import com.yucox.pillpulse.utils.AlarmUtils
import com.yucox.pillpulse.model.AlarmRealm
import com.yucox.pillpulse.databinding.AddReminderActivityBinding
import com.yucox.pillpulse.viewmodel.AlarmViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel

@AndroidEntryPoint
class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding: AddReminderActivityBinding
    private val viewModel: AlarmViewModel by viewModels()
    private lateinit var listAlarmAdapter: AlarmAdapter
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddReminderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadAds()
        initBannerAd()
        infoAboutAlarm()

        viewModel.screenControl.observe(this) {
            if (it == true) {
                binding.screenCl.isClickable = true
            } else {
                binding.screenCl.isClickable = false
            }
        }


        viewModel.message.observe(this) {
            if (it.isEmpty())
                return@observe
            Toast.makeText(
                this,
                it,
                Toast.LENGTH_LONG
            ).show()
        }

        viewModel.rmAlarmList.observe(
            this@AddReminderActivity
        ) {
            if (it != null) {
                setAdapter(it)
                viewModel.reOpenAlarms(this@AddReminderActivity)
            }
        }

        binding.headerContent.btnBackChartCont.setOnClickListener {
            finish()
        }

        binding.goToCreateAlarmBtn.setOnClickListener {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            val isFragmentDisplayed = supportFragmentManager.findFragmentById(R.id.addAlarmFragment)
            if (isFragmentDisplayed == null) {
                fragmentTransaction.add(R.id.addAlarmFragment, CreateAlarmFragment())
                    .addToBackStack(null)
                binding.listAlarmRv.visibility = View.GONE
                binding.addAlarmFragment.visibility = View.VISIBLE
                fragmentTransaction.commit()
            }

        }

        binding.headerContent.permissionBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }

    }

    private fun infoAboutAlarm() {
        val sharedPreferences = getSharedPreferences("checkPermission", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val permissionControl = sharedPreferences.getBoolean("permission", false)

        if (!permissionControl) {
            MaterialAlertDialogBuilder(this)
                .setTitle(
                    "Lütfen ayarlardan uygulamaya izin verin, " +
                            "yoksa alarm özelliği düzgün çalışmayacaktır"
                )
                .setNegativeButton("Ayarlar") { _, _ ->
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
                .setPositiveButton("İzin verdim") { _, _ ->
                    editor.putBoolean("permission", true)
                        .apply()
                }
                .show()
        }

    }

    private fun initBannerAd() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun setAdapter(alarmRealms: List<AlarmRealm>) {

        listAlarmAdapter = AlarmAdapter(
            this@AddReminderActivity,
            alarmRealms.toMutableList(),
            deleteAlarm = { alarm ->
                AlarmUtils(this).closeTheAlarm(alarm)
                viewModel.deleteAlarmLocale(alarm.id)
            },
            closeAlarm = { alarm ->
                AlarmUtils(this).closeTheAlarm(alarm)
                viewModel.changeAlarmStatusLocale(alarm.id)
            }, openAlarm = { alarm ->
                AlarmUtils(this).openTheAlarm(alarm)
                viewModel.changeAlarmStatusLocale(alarm.id)
            }
        )
        binding.listAlarmRv.layoutManager =
            LinearLayoutManager(
                this@AddReminderActivity,
                RecyclerView.VERTICAL,
                false
            )
        binding.listAlarmRv.adapter = listAlarmAdapter
        listAlarmAdapter.notifyDataSetChanged()
    }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-5841174734258930/8252179828",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    loadAds()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    println("yüklendi")
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun showAds() {
        if (mInterstitialAd == null)
            loadAds()
        mInterstitialAd?.show(this)
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null
                loadAds()
            }

            override fun onAdShowedFullScreenContent() {
                loadAds()
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.addAlarmFragment) != null) {
            binding.listAlarmRv.visibility = View.VISIBLE
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        viewModel.viewModelScope.cancel()
        super.onDestroy()
    }
}