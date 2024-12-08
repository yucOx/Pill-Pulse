package com.yucox.pillpulse.presentation.view.pill

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieData
import com.yucox.pillpulse.presentation.state.MainState
import com.yucox.pillpulse.R
import com.yucox.pillpulse.presentation.viewmodel.PillViewModel
import com.yucox.pillpulse.databinding.ActivityChartBinding
import com.yucox.pillpulse.util.ChartUIConstants
import com.yucox.pillpulse.util.gone
import com.yucox.pillpulse.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    @Inject lateinit var viewModel: PillViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupObservers()
        viewModel.loadInitialForChartActivity()
    }

    private fun setupObservers() {
        viewModel.state.observe(this) {
            handleState(it)
        }
    }

    private fun handleState(state: MainState) {
        if (!state.isLoading)
            binding.progressBar3.gone()
        else
            binding.progressBar3.visible()
        if (state.allPills.isNotEmpty()) {
            initChart()
        }
    }

    private fun setupUI() {
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSelectMonthAdapter()
        setupBackButton()
    }

    private fun setupBackButton() {
        val backBtn = findViewById<ImageButton>(R.id.btnBackChartCont)
        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun initSelectMonthAdapter() {
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            ChartUIConstants.MONTHS
        )
        binding.listItemAutoComplete.setAdapter(adapter)
        binding.listItemAutoComplete.setOnItemClickListener { _, _, selectedIndex, _ ->
            lifecycleScope.launch {
                when (selectedIndex) {
                    ChartUIConstants.MONTHS.lastIndex -> initChart()
                    else -> initChart(selectedIndex)
                }
            }
            binding.listItemAutoComplete.hint = ChartUIConstants.MONTHS[selectedIndex]
        }
    }

    private fun initChart(requestedMonth: Int = -1) {
        val pieDataSet = viewModel.setupChart(requestedMonth + 1)
        setPieChartSettings(PieData(pieDataSet), requestedMonth)
    }


    private fun setPieChartSettings(
        pieData: PieData,
        selectedIndex: Int
    ) {
        binding.pieChart.description.text = ""
        binding.pieChart.data = pieData
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.centerText = when (selectedIndex) {
            -1 -> "Toplam Kullanım"
            else -> "${ChartUIConstants.MONTHS[selectedIndex]} Ayı Kullanım"
        }

        binding.pieChart.setCenterTextSize(ChartUIConstants.CENTER_TEXT_SIZE)
        binding.pieChart.description.textSize = ChartUIConstants.DESCRIPTION_TEXT_SIZE
        binding.pieChart.animateY(2000)
        binding.pieChart.visibility = View.VISIBLE
    }
}