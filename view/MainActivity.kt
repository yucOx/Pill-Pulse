package com.yucox.pillpulse.view

import ListPillAdapter
import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hieupt.android.standalonescrollbar.attachTo
import com.yucox.pillpulse.model.ConstValues
import com.yucox.pillpulse.model.PillRealm
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.ActivityMainBinding
import com.yucox.pillpulse.utils.PermissionUtils
import com.yucox.pillpulse.viewmodel.PillViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: PillViewModel by viewModels()
    private lateinit var adapter: ListPillAdapter
    private val _mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingProcess()

        val requestPermissionLauncher = PermissionUtils().showPermissionsRequest(this)

        viewModel.message.observe(this) {
            if (it.isNotEmpty()) {
                Toast.makeText(
                    this,
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.mainContent.buttonMenuDate.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.nvDateMenu)
        }

        binding.mainContent.buttonMenuDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationView)
        }

        confSlider()

        binding.imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                when (position) {
                    0 -> {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.yucox.tinatina")
                        )
                        startActivity(intent)
                    }

                    1 -> {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.yucox.whatthenads")
                        )
                        startActivity(intent)
                    }
                }
            }

            override fun doubleClick(position: Int) {
            }
        })

        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navBackup -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Veriler eşitlenecek onaylıyor musunuz?")
                        .setNegativeButton("Evet") { _, _ ->
                            viewModel.synchronizePills()
                        }
                        .setPositiveButton("Hayır") { _, _ -> }
                        .show()
                }

                R.id.navExit -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Hesabınızdan çıkış yapılacak, onaylıyor musunuz?")
                        .setNegativeButton("Evet") { _, _ ->
                            logOut()
                        }
                        .setPositiveButton("İptal") { _, _ -> }
                        .show()
                }
            }
            binding.drawerLayout.close()
            false
        }

        viewModel.rmPillList.observe(this@MainActivity) {
            if (it.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                viewModel.listPillsByMonth(currentMonth)
            }
        }
        delayMe(1000) {
            binding.progressBar2.visibility = View.GONE
            if (viewModel.rmPillList.value?.isEmpty() == true) {
                val emptyList = mutableListOf<PillRealm>()
                emptyList.add(
                    PillRealm().apply {
                        this.drugName = "Henüz ilaç eklemediniz"
                    }
                )
                initRecycler(emptyList)
            }
        }

        viewModel.specifiedMonthPills.observe(this@MainActivity) {
            if (it.isNotEmpty()) {
                initRecycler(it)
            }
        }

        checkPermissions(requestPermissionLauncher)

        binding.nvDateMenu.setNavigationItemSelectedListener { item ->
            val itemId = item.itemId
            when (itemId) {
                R.id.btn_january -> {
                    viewModel.listPillsByMonth(0)
                }

                R.id.btn_february -> {
                    viewModel.listPillsByMonth(1)
                }

                R.id.btn_march -> {
                    viewModel.listPillsByMonth(2)
                }

                R.id.btn_april -> {
                    viewModel.listPillsByMonth(3)
                }

                R.id.btn_may -> {
                    viewModel.listPillsByMonth(4)
                }

                R.id.btn_june -> {
                    viewModel.listPillsByMonth(5)
                }

                R.id.btn_july -> {
                    viewModel.listPillsByMonth(6)
                }

                R.id.btn_august -> {
                    viewModel.listPillsByMonth(7)
                }

                R.id.btn_september -> {
                    viewModel.listPillsByMonth(8)
                }

                R.id.btn_october -> {
                    viewModel.listPillsByMonth(9)
                }

                R.id.btn_november -> {
                    viewModel.listPillsByMonth(10)
                }

                R.id.btn_december -> {
                    viewModel.listPillsByMonth(11)
                }

                R.id.menu_showall -> {
                    viewModel.listAllYear()
                }
            }
            binding.drawerLayout.closeDrawer(binding.nvDateMenu)
            false
        }

        viewModel.user.observe(this) {
            if (it != null) {
                val welcomeTextView = findViewById<TextView>(R.id.welcomeTv)
                welcomeTextView.text =
                    "Hoş geldin\n${it.name + " " + it.surname}"
            }
        }

        binding.addReminderBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, AddReminderActivity::class.java)
            startActivity(intent)
        }

        binding.addPillBtn.setOnClickListener {
            val intent = Intent(this, AddSinglePİllActivity::class.java)
            startActivity(intent)
        }

        binding.openChartBtn.setOnClickListener {
            openChart()
        }
    }

    private fun confSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.ads_tinatina,"Tina Tina ile not al, gününü planla. Ulaşmak için tıkla!", scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.ads_whatthenads,"İstediğin her dizi WhatTheNads ile seninle. Ulaşmak için tıkla!", scaleType = ScaleTypes.FIT))
        binding.imageSlider.setImageList(imageList)
    }

    private fun logOut() {
        viewModel.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun delayMe(time: Long, ready: () -> Unit) {
        _mainScope.launch {
            delay(time)
            ready()
        }
    }

    private fun initRecycler(specifiedMonthPillsRealm: MutableList<PillRealm>) {
        adapter = ListPillAdapter(
            this@MainActivity,
            specifiedMonthPillsRealm,
            removePill = { id ->
                viewModel.deletePillLocale(id)
            }
        )
        binding.listPillsRecycler.layoutManager = GridLayoutManager(
            this@MainActivity,
            2
        )

        binding.listPillsRecycler.adapter = adapter
        binding.scrollbar.attachTo(binding.listPillsRecycler)
        binding.scrollbar.customThumbDrawable = getDrawable(R.drawable.customscrollbar)

    }

    private fun checkPermissions(
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>,
    ) {
        if (!this.viewModel.checkPermissions(this)) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.SET_ALARM,
                    Manifest.permission.USE_EXACT_ALARM,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                )
            )
        } else {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(
                rootView,
                "Uygulamanın bildirim ve alarm izinlerini ayarlardan" +
                        "ayarlamazsanız uygulama düzgün çalışmayacaktır.",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Tamam") {
            }.show()
        }
    }

    private fun openChart() {
        if (binding.progressBar2.visibility == View.VISIBLE) {
            val rootView = findViewById<View>(android.R.id.content)
            Snackbar.make(
                rootView,
                "Lütfen bütün bilgiler yüklenirken bekleyin ve bir daha deneyin.",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        val intent = Intent(
            this@MainActivity,
            ChartActivity::class.java
        )
        startActivity(intent)
    }


    private fun loadingProcess() {
        val mainScope = CoroutineScope(Dispatchers.Main)
        binding.progressBar2.progress += 10
        mainScope.launch {
            if (binding.progressBar2.progress < 100 && binding.progressBar2.visibility == View.VISIBLE) {
                delay(300)
                loadingProcess()
                println(binding.progressBar2.progress)
            } else {
                binding.progressBar2.progress = 80
            }
            mainScope.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _mainScope.cancel()
        viewModel.viewModelScope.cancel()
    }
}
