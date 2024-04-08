package com.yucox.pillpulse.View

import ListPillAdapter
import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hieupt.android.standalonescrollbar.attachTo
import com.wynneplaga.materialScrollBar2.MaterialScrollBar
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.ActivityMainBinding
import com.yucox.pillpulse.ViewModel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ListPillAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingProcces()
        initBannerAd()

        val requestPermissionLauncher = initRequestPermission(this)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.fetchUserInfo(viewModel)

        viewModel.fetchPills(viewModel)

        viewModel.pillList.observe(this@MainActivity) {
            if (it.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                viewModel.listSpecifiedMonth(currentMonth)
            }
        }

        viewModel.specifiedMonthPills.observe(this@MainActivity) {
            if (it.isNotEmpty()) {
                initRecycler()
                binding.progressBar2.visibility = View.GONE
            }
        }

        checkPermissions(requestPermissionLauncher)

        viewModel.user.observe(this) {
            if (it != null) {
                binding.welcomeTv.text =
                    "Hoş geldin\n${it.name + " " + it.surname}"
            }
        }

        binding.addReminderBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, AddReminderActivity::class.java)
            startActivity(intent)
        }


        binding.addTimeBtn.setOnClickListener {
            val intent = Intent(this, AddTimeActivity::class.java)
            intent.putExtra("pillDetails", viewModel.pillList.value)
            startActivity(intent)
        }

        openChart()

        listByMonth()

        logOut()

    }

    private fun listByMonth() {
        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, months)
        binding.listItemAutoComplete.setAdapter(adapter)
        binding.listItemAutoComplete.setOnItemClickListener { _, _, selectedIndex, _ ->
            if (selectedIndex == 12) {
                viewModel.listAllYear()
            } else {
                viewModel.listSpecifiedMonth(selectedIndex)
            }
        }
    }

    private fun initRecycler() {
        adapter = ListPillAdapter(
            this@MainActivity,
            viewModel.specifiedMonthPills
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

    private fun initRequestPermission(context: Context): ActivityResultLauncher<Array<String>> {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            ActivityResultCallback { permissions ->
                if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true
                    &&
                    permissions[Manifest.permission.SET_ALARM] == true
                    &&
                    permissions[Manifest.permission.USE_EXACT_ALARM] == true
                    &&
                    permissions[Manifest.permission.SCHEDULE_EXACT_ALARM] == true
                ) {
                } else {
                    val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as MainActivity,
                                Manifest.permission.SET_ALARM
                            ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as MainActivity,
                                Manifest.permission.USE_EXACT_ALARM
                            ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                context as MainActivity,
                                Manifest.permission.SCHEDULE_EXACT_ALARM
                            )
                    if (rationaleRequired) {
                        Toast.makeText(
                            context,
                            "Programın düzgün çalışması için izinleri aktif etmeniz gerekli",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }
            }
        )
        return requestPermissionLauncher
    }

    private fun openChart() {
        binding.openChartBtn.setOnClickListener {
            if (binding.progressBar2.visibility == View.VISIBLE) {
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(
                    rootView,
                    "Lütfen bütün bilgiler yüklenirken bekleyin ve bir daha deneyin.",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(
                this@MainActivity,
                ChartActivity::class.java
            )
            intent.putExtra(
                "pillDetails",
                viewModel.pillList.value
            )
            startActivity(intent)
        }
    }

    private fun initBannerAd() {
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun logOut() {
        binding.profileIv.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Çıkış yapmak istiyor musunuz?")
                .setNegativeButton("Evet") { _, _ ->
                    viewModel.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setPositiveButton("Hayır") { _, _ -> }
                .show()
        }
    }


    private fun loadingProcces() {
        binding.progressBar2.progress += 10
        CoroutineScope(Dispatchers.Main).launch {
            if (binding.progressBar2.progress < 100 && binding.progressBar2.visibility == View.VISIBLE) {
                delay(300)
                loadingProcces()
                println(binding.progressBar2.progress)
            } else {
                binding.progressBar2.progress = 80
            }
        }
    }

    override fun onRestart() {
        binding.progressBar2.visibility = View.VISIBLE
        loadingProcces()
        viewModel.fetchPills(viewModel)
        super.onRestart()
    }

    companion object {
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
            "Aralık",
            "Hepsini göster"
        )
    }
}
