package com.yucox.pillpulse.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.AddTimeActivityBinding
import com.yucox.pillpulse.viewmodel.PillViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddTimeActivity : AppCompatActivity() {
    private lateinit var binding: AddTimeActivityBinding

    @Inject
    lateinit var viewModel: PillViewModel
    lateinit var mAdView: AdView
    private var pastPills: MutableList<String>? = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.message.observe(this) {
            if (it.isNotEmpty()) {
                showToast(it)
            }
        }

        viewModel.onProcess.observe(this) {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setupUI() {
        binding = AddTimeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBannerAd(binding.adView)

        binding.saveBtn.setOnClickListener {
            val status = viewModel.onProcess.value
            if (status == true)
                return@setOnClickListener

            lifecycleScope.launch {
                val pillName = binding.pillNameText.text?.toString()
                if (pillName.isNullOrEmpty())
                    return@launch
                viewModel.savePillToData(
                    pillName,
                    time = null,
                    ""
                )
            }
        }

        viewModel.allPills.value?.let { pillList ->
            if (pillList.isEmpty()) {
                pastPills?.add("Daha önce kaydettiğiniz bir ilaç bulunamadı.")
                initPastPillsAdapter()
            } else {
                pastPills = viewModel.lastUsedNameOfPills()
                initPastPillsAdapter()
            }
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
    }

    private fun initPastPillsAdapter() {
        val arrayAdapter =
            ArrayAdapter<String>(
                this@AddTimeActivity,
                R.layout.special_list_item,
                pastPills?.toList().orEmpty()
            )
        binding.listView.adapter = arrayAdapter
        binding.listView.setOnItemClickListener { p0, p1, p2, p3 ->
            binding.pillNameText.setText(pastPills?.get(p2)?.toString())
            binding.listView.visibility = View.GONE
        }
        binding.listView.visibility = View.GONE
    }


    private fun initBannerAd(adView: AdView) {
        MobileAds.initialize(this) {}
        mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
}