package com.yucox.pillpulse.presentation.view.pill

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.yucox.pillpulse.presentation.effect.MainEffect
import com.yucox.pillpulse.presentation.event.MainEvent
import com.yucox.pillpulse.presentation.state.MainState
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.AddTimeActivityBinding
import com.yucox.pillpulse.presentation.viewmodel.PillViewModel
import com.yucox.pillpulse.util.gone
import com.yucox.pillpulse.util.showToast
import com.yucox.pillpulse.util.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddTimeActivity : AppCompatActivity() {
    private lateinit var binding: AddTimeActivityBinding

    @Inject
    lateinit var viewModel: PillViewModel
    lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupListeners()
        setupObservers()
        viewModel.loadInitialDataForAddTime()
    }


    private fun savePill() {
        val pillName = binding.pillNameText.text.toString()
        viewModel.onEvent(MainEvent.SavePill(pillName))
    }

    private fun showOrHidePastPills() {
        if (binding.listViewPastPills.visibility == View.GONE) {
            binding.listViewPastPills.visible()
        } else {
            binding.listViewPastPills.gone()
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            updateUI(state)
        }
        viewModel.effect.observe(this) { effect ->
            handleEffect(effect)
        }
    }

    private fun setupListeners() {
        binding.saveBtn.setOnClickListener {
            savePill()
        }
        binding.contentSinglePill.btnBackChartCont.setOnClickListener {
            finish()
        }
        binding.selectPastConst.setOnClickListener {
            showOrHidePastPills()
        }
    }

    private fun handleEffect(effect: MainEffect) {
        when (effect) {
            is MainEffect.ShowToast -> showToast(effect.message)
            MainEffect.NavigateToAddPill -> return
            MainEffect.NavigateToAddReminder -> return
            MainEffect.NavigateToChart -> return
            MainEffect.NavigateToLogin -> return
        }
    }

    private fun updateUI(state: MainState) {
        when (state.isLoading) {
            true -> binding.progressBar.visible()
            false -> binding.progressBar.gone()
        }
        if(state.pastPills.isNotEmpty()){
            initPastPillsAdapter(state.pastPills)
        }
    }


    private fun setupUI() {
        binding = AddTimeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBannerAd(binding.adView)
    }

    private fun initPastPillsAdapter(pastPills: List<String>) {
        val arrayAdapter =
            ArrayAdapter<String>(
                this@AddTimeActivity,
                R.layout.special_list_item,
                pastPills
            )
        binding.listViewPastPills.adapter = arrayAdapter
        binding.listViewPastPills.setOnItemClickListener { p0, p1, p2, p3 ->
            binding.pillNameText.setText(pastPills?.get(p2)?.toString())
            binding.listViewPastPills.gone()
        }
        binding.listViewPastPills.gone()
    }


    private fun initBannerAd(adView: AdView) {
        MobileAds.initialize(this) {}
        mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}