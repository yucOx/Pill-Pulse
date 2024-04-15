package com.yucox.pillpulse.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.yucox.pillpulse.Messages.UNEXPECTEDERROR
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.AddTimeActivityBinding
import com.yucox.pillpulse.ViewModel.MainViewModel
import com.yucox.pillpulse.Model.PillTime

class AddTimeActivity : AppCompatActivity() {
    private lateinit var binding: AddTimeActivityBinding
    private lateinit var viewModel: MainViewModel
    lateinit var mAdView: AdView
    private var pillDetails = ArrayList<PillTime>()
    private var pastPills = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddTimeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pillDetails = intent.getSerializableExtra("pillDetails") as ArrayList<PillTime>

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        selectFromPast()

        viewModel.saveCheck.observe(this) {
            if (it) {
                finish()
            } else {
                Toast.makeText(
                    this,
                    UNEXPECTEDERROR,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.saveBtn.setOnClickListener {
            viewModel.saveNewPill(
                binding.pillNameText.text.toString(),
                binding.noteEt.text.toString(),
            )
        }

        binding.backIv.setOnClickListener {
            finish()
        }
        initBannerAd(binding.adView)
    }

    private fun initPastPillsAdapter() {
        val arrayAdapter =
            ArrayAdapter<String>(
                this@AddTimeActivity,
                R.layout.special_list_item,
                pastPills
            )
        binding.listView.adapter = arrayAdapter
        binding.listView.setOnItemClickListener { p0, p1, p2, p3 ->
            binding.pillNameText.setText(pastPills[p2])
            binding.listView.visibility = View.GONE
        }
        binding.listView.visibility = View.GONE
        binding.selectPastConst.setOnClickListener {
            if (binding.listView.visibility == View.GONE) {
                binding.listView.visibility = View.VISIBLE
            } else {
                binding.listView.visibility = View.GONE
            }
        }
    }

    private fun selectFromPast() {
        if (pillDetails.isEmpty()) {
            viewModel.fetchPills()
            viewModel.pillList.observe(this) {
                if (it.isNullOrEmpty()) {
                    pastPills.add("Daha önce kaydettiğiniz bir ilaç bulunamadı.")
                    initPastPillsAdapter()
                } else {
                    pastPills = viewModel.listPillsByOrderedName()
                    initPastPillsAdapter()
                }
            }
        } else {
            viewModel.updatePillList(pillDetails)
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

    override fun onDestroy() {
        super.onDestroy()
    }
}