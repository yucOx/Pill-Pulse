package com.yucox.pillpulse.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.AddSinglePillActivityBinding
import com.yucox.pillpulse.viewmodel.PillViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddSinglePİllActivity : AppCompatActivity() {
    private lateinit var binding: AddSinglePillActivityBinding
    private val viewModel: PillViewModel by viewModels()
    lateinit var mAdView: AdView
    private var pastPills = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddSinglePillActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.rmPillList.observe(this) {
            selectFromPast()
        }

        binding.saveBtn.setOnClickListener {
            val pillName = binding.pillNameText.text?.toString()
            if (pillName.isNullOrEmpty())
                return@setOnClickListener
            viewModel.savePillLocale(
                pillName,
                null,
                ""
            )
            finish()
        }

        binding.contentSinglePill.btnBackChartCont.setOnClickListener {
            finish()
        }

        binding.selectPastConst.setOnClickListener {
            if (binding.listView.visibility == View.GONE) {
                binding.listView.visibility = View.VISIBLE
            } else {
                binding.listView.visibility = View.GONE
            }
        }

        initBannerAd(binding.adView)
    }

    private fun initPastPillsAdapter() {
        val arrayAdapter =
            ArrayAdapter<String>(
                this@AddSinglePİllActivity,
                R.layout.special_list_item,
                pastPills
            )
        binding.listView.adapter = arrayAdapter
        binding.listView.setOnItemClickListener { p0, p1, p2, p3 ->
            binding.pillNameText.setText(pastPills[p2])
            binding.listView.visibility = View.GONE
        }
        binding.listView.visibility = View.GONE
    }

    private fun selectFromPast() {
        val pillList = viewModel.rmPillList.value
        if (pillList?.isEmpty() == true) {
            pastPills.add("Daha önce kaydettiğiniz bir ilaç bulunamadı.")
            initPastPillsAdapter()
        } else {
            pastPills = viewModel.listPillsByOrderedName()
            initPastPillsAdapter()
        }
    }

    private fun initBannerAd(adView: AdView) {
        MobileAds.initialize(this) {}
        mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}