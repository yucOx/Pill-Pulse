package com.yucox.pillpulse.view

import ListPillAdapter
import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hieupt.android.standalonescrollbar.attachTo
import com.yucox.pillpulse.model.PillRealm
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.ActivityMainBinding
import com.yucox.pillpulse.model.Pill
import com.yucox.pillpulse.util.TimeUtils
import com.yucox.pillpulse.utils.PermissionUtils
import com.yucox.pillpulse.viewmodel.LoginViewModel
import com.yucox.pillpulse.viewmodel.PillViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var viewModel: PillViewModel

    private val authViewModel: LoginViewModel by viewModels()
    private lateinit var adapter: ListPillAdapter
    private val _mainScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setupObservers()

        val requestPermissionLauncher = PermissionUtils().showPermissionsRequest(this)
        checkPermissions(requestPermissionLauncher)

    }

    private fun setupObservers() {
        viewModel.message.observe(this) {
            if (it.isNotEmpty()) {
                Toast.makeText(
                    this,
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.onProcess.observe(this) {
            if (it == false)
                binding.progressBar2.visibility = View.GONE
            else
                binding.progressBar2.visibility = View.VISIBLE
        }

        viewModel.pillListForOneMonth.observe(this@MainActivity) {
            if (it.isNullOrEmpty()) {
                adapter.pillList = ArrayList<Pill>().apply {
                    add(
                        Pill(
                            drugName = "Henüz ilaç eklemediniz",
                            whenYouTookHour = TimeUtils.toStringClock(Date()),
                            whenYouTookDate = TimeUtils.toStringCalendar(Date())
                        )
                    )
                }
                adapter.notifyDataSetChanged()
                return@observe
            }
            adapter.pillList = it
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecycler()

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

        binding.nvDateMenu.setNavigationItemSelectedListener { item ->
            val itemId = item.itemId
            lifecycleScope.launch {
                when (itemId) {
                    R.id.btn_january -> {
                        viewModel.getPillsPaginated(1)
                    }

                    R.id.btn_february -> {
                        viewModel.getPillsPaginated(2)
                    }

                    R.id.btn_march -> {
                        viewModel.getPillsPaginated(3)
                    }

                    R.id.btn_april -> {
                        viewModel.getPillsPaginated(4)
                    }

                    R.id.btn_may -> {
                        viewModel.getPillsPaginated(5)
                    }

                    R.id.btn_june -> {
                        viewModel.getPillsPaginated(6)
                    }

                    R.id.btn_july -> {
                        viewModel.getPillsPaginated(7)
                    }

                    R.id.btn_august -> {
                        viewModel.getPillsPaginated(8)
                    }

                    R.id.btn_september -> {
                        viewModel.getPillsPaginated(9)
                    }

                    R.id.btn_october -> {
                        viewModel.getPillsPaginated(10)
                    }

                    R.id.btn_november -> {
                        viewModel.getPillsPaginated(11)
                    }

                    R.id.btn_december -> {
                        viewModel.getPillsPaginated(12)
                    }

                    R.id.menu_showall -> {
                        viewModel.allPills.value?.let {
                            adapter.pillList = it
                        }
                    }
                }

            }
            binding.drawerLayout.closeDrawer(binding.nvDateMenu)
            false
        }

        binding.addReminderBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, AddReminderActivity::class.java)
            startActivity(intent)
        }

        binding.addPillBtn.setOnClickListener {
            val intent = Intent(this, AddTimeActivity::class.java)
            startActivity(intent)
        }

        binding.openChartBtn.setOnClickListener {
            openChart()
        }
    }

    private fun confSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(
            SlideModel(
                R.drawable.ads_tinatina,
                "Tina Tina ile not al, gününü planla. Ulaşmak için tıkla!",
                scaleType = ScaleTypes.FIT
            )
        )
        imageList.add(
            SlideModel(
                R.drawable.ads_whatthenads,
                "İstediğin her dizi WhatTheNads ile seninle. Ulaşmak için tıkla!",
                scaleType = ScaleTypes.FIT
            )
        )
        binding.imageSlider.setImageList(imageList)
    }

    private fun logOut() {
        authViewModel.logOut()
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

    private fun initRecycler() {
        val emptyList = mutableListOf<Pill>().apply {
            add(Pill(drugName = "Henüz ilaç eklemediniz"))
        }
        adapter = ListPillAdapter(
            this@MainActivity,
            emptyList,
            removePill = { id ->
                viewModel.deletePill(id)
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
}