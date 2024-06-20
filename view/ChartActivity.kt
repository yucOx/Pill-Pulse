package com.yucox.pillpulse.view

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.activity.viewModels
import com.github.mikephil.charting.data.PieData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.yucox.pillpulse.R
import com.yucox.pillpulse.model.ConstValues.months
import com.yucox.pillpulse.viewmodel.PillViewModel
import com.yucox.pillpulse.databinding.ActivityChartBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    private val viewModel: PillViewModel by viewModels()
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadAds()

        viewModel.rmPillList.observe(this) {
            if (it.isNotEmpty()) {
                initChart()
            }
        }

        showByMonths()

        val backBtn = findViewById<ImageButton>(R.id.btnBackChartCont)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showByMonths() {
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            months
        )
        binding.listItemAutoComplete.setAdapter(adapter)
        binding.listItemAutoComplete.setOnItemClickListener { _, _, selectedIndex, _ ->
            showAds()
            if (selectedIndex == 12) {
                viewModel.listAllYear()
                initChart()
            } else {
                viewModel.listPillsByMonth(selectedIndex)
                initChartBySpecifiedMonth(selectedIndex)
            }
            binding.listItemAutoComplete.setHint(months[selectedIndex])

        }
    }

    private fun initChart() {
        binding.progressBar3.progress = 80
        val pieDataSet = viewModel.prepareChartData()
        val pieData = PieData(pieDataSet)
        setPieChartSettings(pieData, -1)

        binding.progressBar3.progress = 100
        binding.progressBar3.visibility = View.GONE
    }

    private fun initChartBySpecifiedMonth(selectedIndex: Int) {
        binding.progressBar3.progress = 80
        val pieDataSet = viewModel.prepareChartDataForSpecifiedMonth()
        val pieData = PieData(pieDataSet)
        setPieChartSettings(pieData, selectedIndex)

        binding.progressBar3.progress = 100
        binding.progressBar3.visibility = View.GONE
    }

    private fun setPieChartSettings(
        pieData: PieData,
        selectedIndex: Int
    ) {
        binding.pieChart.description.text = ""
        binding.pieChart.data = pieData
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        println(viewModel.specifiedMonthPills.value?.size)
        if (selectedIndex == -1) {
            binding.pieChart.centerText = "Toplam Kullanım"
        } else {
            binding.pieChart.centerText = "${months[selectedIndex]} Ayı Kullanım"
        }
        binding.pieChart.setCenterTextSize(15f)
        binding.pieChart.description.textSize = 16f
        binding.pieChart.animateY(2000)
        binding.pieChart.visibility = View.VISIBLE
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
            }
        }
    }

}