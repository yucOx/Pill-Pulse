package com.yucox.pillpulse.view

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.yucox.pillpulse.R
import com.yucox.pillpulse.viewmodel.PillViewModel
import com.yucox.pillpulse.databinding.ActivityChartBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    private val viewModel: PillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.allPills.observe(this) {
            if (it.isNotEmpty()) {
                initChart()
            }
        }

        viewModel.onProcess.observe(this) {
            if (it == false)
                binding.progressBar3.visibility = View.GONE
            else
                binding.progressBar3.visibility = View.VISIBLE
        }
    }

    private fun setupUI() {
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showByMonths()

        viewModel.allPills.value?.let {
            if (it.isNotEmpty()) {
                initChart()
            }
        }

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
            if (selectedIndex == 12) {
                initChart()
            } else {
                lifecycleScope.launch {
                    viewModel.getPillsPaginated(selectedIndex + 1)
                    initChartBySpecifiedMonth(selectedIndex)
                }
            }
            binding.listItemAutoComplete.setHint(months[selectedIndex])
        }
    }

    private fun initChart() {
        lifecycleScope.launch {
            val pieDataSet = viewModel.prepareChartData()
            val pieData = PieData(pieDataSet)
            setPieChartSettings(pieData, -1)
        }
    }

    private suspend fun initChartBySpecifiedMonth(selectedIndex: Int) {
        val pieDataSet = viewModel.prepareChartDataForSpecifiedMonth()
        val pieData = PieData(pieDataSet)
        setPieChartSettings(pieData, selectedIndex)

    }

    private fun setPieChartSettings(
        pieData: PieData,
        selectedIndex: Int
    ) {
        binding.pieChart.description.text = ""
        binding.pieChart.data = pieData
        binding.pieChart.setEntryLabelColor(Color.BLACK)
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

    val months = arrayOf(
        "Ocak",
        "Şubat",
        "Mart",
        "Nisan",
        "Mayıs",
        "Haziran",
        "Temmuz",
        "Ağustos",
        "Eylül",
        "Ekim",
        "Kasım",
        "Aralık",
        "Hepsini göster"
    )
}