package com.yucox.pillpulse.presentation.view.alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.yucox.pillpulse.R
import com.yucox.pillpulse.presentation.adapter.AlarmAdapter
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.databinding.AddReminderActivityBinding
import com.yucox.pillpulse.presentation.effect.AlarmEffect
import com.yucox.pillpulse.presentation.event.AlarmEvent
import com.yucox.pillpulse.presentation.state.AlarmState
import com.yucox.pillpulse.presentation.viewmodel.AlarmViewModel
import com.yucox.pillpulse.util.gone
import com.yucox.pillpulse.util.showToastLong
import com.yucox.pillpulse.util.visible
import com.yucox.pillpulse.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding: AddReminderActivityBinding
    private val viewModel: AlarmViewModel by viewModels()
    private lateinit var listAlarmAdapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupObservers()
        setupListeners()
        askPermissions()
        initBannerAd()

    }

    private fun setupUI() {
        binding = AddReminderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupListeners() {
        with(binding) {
            btnCreateAlarm.setOnClickListener {
                if (!isAlarmFragmentVisible()) {
                    showCreateAlarmFragment()
                }
            }

            headerContent.apply {
                permissionBtn.setOnClickListener {
                    navigateToAlarmSettings()
                }

                btnBackChartCont.setOnClickListener {
                    finish()
                }
            }
        }
    }

    private fun navigateToAlarmSettings() {
        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
    }

    private fun isAlarmFragmentVisible(): Boolean {
        val isVisible = supportFragmentManager.findFragmentById(R.id.addAlarmFragment)
        return isVisible != null
    }

    private fun showCreateAlarmFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.addAlarmFragment, CreateAlarmFragment())
            .addToBackStack(null)
        binding.listAlarmRv.gone()
        binding.addAlarmFragment.visible()
        fragmentTransaction.commit()
    }

    private fun setupObservers() {
        viewModel.state.observe(this) {
            handleState(it)
        }
        viewModel.effect.observe(this) {
            handleEffects(it)

        }
    }

    private fun handleEffects(effect: AlarmEffect) {
        when (effect) {
            is AlarmEffect.ShowToast -> showToastLong(effect.message)
        }
    }

    private fun handleState(state: AlarmState) {
        if (state.alarmList.isEmpty())
            return
        setAdapter(state.alarmList)
    }


    private fun askPermissions() {
        val permissionUtils = PermissionUtils()
        val permissionLauncher = permissionUtils.showPermissionsRequest(this)

        if (!permissionUtils.hasPermission(this)) {
            permissionUtils.requestPermissions(permissionLauncher)
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
                viewModel.onEvent(AlarmEvent.DeleteAlarm(alarm.id))
                viewModel.onEvent(AlarmEvent.CancelAlarm(alarm))
            },
            closeAlarm = { alarm ->
                viewModel.onEvent(AlarmEvent.CancelAlarm(alarm))
                viewModel.onEvent(AlarmEvent.ChangeAlarmState(alarm.id))
            }, openAlarm = { alarm ->
                viewModel.onEvent(AlarmEvent.ScheduleAlarm(alarm))
                viewModel.onEvent(AlarmEvent.ChangeAlarmState(alarm.id))
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

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentById(R.id.addAlarmFragment) != null) {
            binding.listAlarmRv.visibility = View.VISIBLE
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

}