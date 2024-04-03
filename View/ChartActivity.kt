package com.yucox.pillpulse.View

import android.R
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.yucox.pillpulse.ViewModel.MainViewModel
import com.yucox.pillpulse.databinding.ActivityChartBinding
import com.yucox.pillpulse.Model.PillTime

class ChartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChartBinding
    private lateinit var viewModel: MainViewModel
    private val months = arrayOf(
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
        "Aralık"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pillDetails = intent.getSerializableExtra("pillDetails") as ArrayList<PillTime>

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.updatePillList(pillDetails)

        val drugCount = viewModel.prepareChartData()
        initChart(drugCount)

        showByMonths()

        binding.backIv.setOnClickListener {
            finish()
        }
    }

    private fun showByMonths() {
        val adapter =
            ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item, months)
        binding.listItemAutoComplete.setAdapter(adapter)
        binding.listItemAutoComplete.setOnItemClickListener { _, _, selectedIndex, _ ->
            viewModel.listSpecifiedMonth(selectedIndex)
            binding.listItemAutoComplete.setHint(months[selectedIndex])
            val drugCount = viewModel.prepareChartDataForSpecifiedMonth()

            initChartBySpecifiedMonth(drugCount,selectedIndex)
        }
    }

    private fun initChart(drugCount: HashMap<String, Float>) {
        val list = ArrayList<PieEntry>()

        binding.progressBar3.progress = 80
        for (a in drugCount) {
            list.add(PieEntry(a.value, a.key))
        }

        val colorsArray = ArrayList<Int>()
        for (a in ColorTemplate.VORDIPLOM_COLORS) {
            colorsArray.add(a)
        }
        for (a in ColorTemplate.JOYFUL_COLORS) {
            colorsArray.add(a)
        }
        val pieDataSet = PieDataSet(list, "")

        pieDataSet.setColors(colorsArray);

        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 18f

        val pieData = PieData(pieDataSet)
        binding.pieChart.description.text = ""
        binding.pieChart.data = pieData
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.centerText = "Toplam Kullanım"
        binding.pieChart.setCenterTextSize(15f)
        binding.pieChart.description.textSize = 16f
        binding.pieChart.animateY(2000)

        binding.pieChart.visibility = View.VISIBLE
        binding.progressBar3.progress = 100
        binding.progressBar3.visibility = View.GONE
    }


    private fun initChartBySpecifiedMonth(drugCount: HashMap<String, Float>, selectedIndex: Int) {
        val list = ArrayList<PieEntry>()

        binding.progressBar3.progress = 80
        for (a in drugCount) {
            list.add(PieEntry(a.value, a.key))
        }

        val colorsArray = ArrayList<Int>()
        for (a in ColorTemplate.VORDIPLOM_COLORS) {
            colorsArray.add(a)
        }
        for (a in ColorTemplate.JOYFUL_COLORS) {
            colorsArray.add(a)
        }
        val pieDataSet = PieDataSet(list, "")

        pieDataSet.setColors(colorsArray);

        pieDataSet.valueTextColor = Color.BLACK
        pieDataSet.valueTextSize = 18f

        val pieData = PieData(pieDataSet)
        binding.pieChart.description.text = ""
        binding.pieChart.data = pieData
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.centerText = "${months[selectedIndex]} Ayı Kullanım"
        binding.pieChart.setCenterTextSize(15f)
        binding.pieChart.description.textSize = 16f
        binding.pieChart.animateY(2000)

        binding.pieChart.visibility = View.VISIBLE
        binding.progressBar3.progress = 100
        binding.progressBar3.visibility = View.GONE
    }

}