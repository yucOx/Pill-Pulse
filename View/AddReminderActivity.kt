package com.yucox.pillpulse.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.pillpulse.R
import com.yucox.pillpulse.Adapter.AlarmAdapter
import com.yucox.pillpulse.databinding.AddReminderActivityBinding
import com.yucox.pillpulse.ViewModel.AlarmViewModel

class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding: AddReminderActivityBinding
    private lateinit var viewModel: AlarmViewModel
    private lateinit var listAlarmAdapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddReminderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBannerAd()
        infoAboutAlarm()

        viewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)

        viewModel.fetchAlarms(viewModel)

        viewModel.alarmList.observe(this@AddReminderActivity) {
            if (it.isNotEmpty()) {
                viewModel.reOpenAlarms(this@AddReminderActivity)
                setAdapter()
            }
        }

        binding.backIv.setOnClickListener {
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

        binding.permissionBtn.setOnClickListener {
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

    private fun setAdapter() {
        listAlarmAdapter = AlarmAdapter(
            this@AddReminderActivity,
            viewModel.alarmList.value!!
        )
        binding.listAlarmRv.layoutManager =
            LinearLayoutManager(
                this@AddReminderActivity,
                RecyclerView.VERTICAL,
                false
            )
        binding.listAlarmRv.adapter = listAlarmAdapter
    }


    override fun onRestart() {
        viewModel.fetchAlarms(viewModel)
        super.onRestart()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.addAlarmFragment) != null) {
            binding.listAlarmRv.visibility = View.VISIBLE
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}