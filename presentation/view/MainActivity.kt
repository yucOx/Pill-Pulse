package com.yucox.pillpulse.presentation.view

import ListPillAdapter
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.PieData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.pillpulse.presentation.effect.MainEffect
import com.yucox.pillpulse.presentation.event.MainEvent
import com.yucox.pillpulse.presentation.state.MainState
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.ActivityMainBinding
import com.yucox.pillpulse.domain.model.Pill
import com.yucox.pillpulse.presentation.view.alarm.AddReminderActivity
import com.yucox.pillpulse.presentation.view.auth.LoginActivity
import com.yucox.pillpulse.presentation.view.pill.AddTimeActivity
import com.yucox.pillpulse.presentation.view.pill.ChartActivity
import com.yucox.pillpulse.utils.PermissionUtils
import com.yucox.pillpulse.presentation.viewmodel.PillViewModel
import com.yucox.pillpulse.util.ChartUIConstants
import com.yucox.pillpulse.util.gone
import com.yucox.pillpulse.util.showToastLong
import com.yucox.pillpulse.util.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject lateinit var viewModel: PillViewModel
    private lateinit var adapter: ListPillAdapter
    private lateinit var permissionUtils: PermissionUtils
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        checkPermissions()
        setupObservers()
    }

    private fun setupUI() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.effect.observe(this) { effect ->
            handleEffects(effect)
        }

        viewModel.state.observe(this) { state ->
            handleStates(state)
        }
    }

    private fun handleStates(state: MainState) {
        when (state.isLoading) {
            true -> binding.progressBar2.visible()
            false -> binding.progressBar2.gone()
        }
        if (state.selectedPills.isNotEmpty()) {
            updateChart(state.selectedMonth)
            if (::adapter.isInitialized)
                adapter.submitList(state.selectedPills)
            else
                setupRecyclerView(state.selectedPills)
        }
    }

    private fun handleEffects(effect: MainEffect) {
        when (effect) {
            is MainEffect.ShowToast -> showToastLong(effect.message)
            is MainEffect.NavigateToAddReminder -> navigateToAddReminder()
            is MainEffect.NavigateToChart -> navigateToChart()
            is MainEffect.NavigateToLogin -> logOut()
            is MainEffect.NavigateToAddPill -> navigateToAddPill()
        }
    }

    private fun navigateToAddPill() {
        val intent = Intent(this@MainActivity, AddTimeActivity::class.java)
        startActivity(intent)
    }

    private fun setupRecyclerView(pills: List<Pill>) {
        adapter = ListPillAdapter(
            this,
            pills.toMutableList(),
            removePill = {
                viewModel.onEvent(MainEvent.DeletePill(it))
            })
        binding.apply {
            listPillsRecycler.adapter = adapter
            listPillsRecycler.layoutManager = GridLayoutManager(
                this@MainActivity,
                2
            )
        }
        createScrollListener()
    }

    private fun createScrollListener() {
        binding.listPillsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                if (shouldLoadMore(layoutManager))
                    viewModel.onEvent(MainEvent.LoadMore())
            }
        })
    }

    private fun shouldLoadMore(layoutManager: GridLayoutManager): Boolean {
        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
        return (visibleItemCount + pastVisibleItems) >= totalItemCount
    }

    private fun updateChart(requestedMonth: Int = -1) {
        viewModel.setupChart(requestedMonth).let { pieDataSet ->
            displayChart(PieData(pieDataSet), requestedMonth)
        }
    }

    private fun displayChart(
        pieData: PieData,
        requestedMonth: Int,
    ) {
        binding.pieChart.description.text = ""
        binding.pieChart.data = pieData
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.centerText = ChartUIConstants.MONTHS[requestedMonth - 1]
        binding.pieChart.setCenterTextSize(13f)
        binding.pieChart.description.textSize = 12f
        binding.pieChart.animateY(2000)
        binding.pieChart.visibility = View.VISIBLE
    }

    private fun navigateToChart() {
        val intent = Intent(this@MainActivity, ChartActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAddReminder() {
        val intent = Intent(this@MainActivity, AddReminderActivity::class.java)
        startActivity(intent)
    }

    private fun setupClickListeners() {
        binding.mainContent.buttonMenuDate.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.nvDateMenu)
        }

        binding.mainContent.buttonMenuDrawer.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationView)
        }

        binding.addPillBtn.setOnClickListener {
            viewModel.onEvent(MainEvent.AddPill)
        }

        binding.openChartBtn.setOnClickListener {
            openChart()
        }

        binding.nvDateMenu.setNavigationItemSelectedListener { item ->
            val itemId = item.itemId
            when (itemId) {
                R.id.btn_january -> {
                    println("1")
                    viewModel.onEvent(MainEvent.MonthSelected(1))
                }

                R.id.btn_february -> {
                    println("1")

                    viewModel.onEvent(MainEvent.MonthSelected(2))
                }

                R.id.btn_march -> {
                    viewModel.onEvent(MainEvent.MonthSelected(3))
                    println("1")
                }

                R.id.btn_april -> {
                    viewModel.onEvent(MainEvent.MonthSelected(4))
                    println("1")
                }

                R.id.btn_may -> {
                    viewModel.onEvent(MainEvent.MonthSelected(5))
                    println("1")
                }

                R.id.btn_june -> {
                    viewModel.onEvent(MainEvent.MonthSelected(6))
                    println("1")
                }

                R.id.btn_july -> {
                    viewModel.onEvent(MainEvent.MonthSelected(7))
                    println("1")
                }

                R.id.btn_august -> {
                    viewModel.onEvent(MainEvent.MonthSelected(8))
                    println("1")
                }

                R.id.btn_september -> {
                    viewModel.onEvent(MainEvent.MonthSelected(9))
                    println("1")
                }

                R.id.btn_october -> {
                    viewModel.onEvent(MainEvent.MonthSelected(10))
                    println("1")
                }

                R.id.btn_november -> {
                    viewModel.onEvent(MainEvent.MonthSelected(11))
                    println("1")
                }

                R.id.btn_december -> {
                    viewModel.onEvent(MainEvent.MonthSelected(12))
                    println("1")
                }

                R.id.navExit -> {
                    viewModel.onEvent(MainEvent.LogOut)
                }
            }
            binding.drawerLayout.closeDrawer(binding.nvDateMenu)
            true
        }
        binding.addReminderBtn.setOnClickListener { viewModel.onEvent(MainEvent.AddReminder) }

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
    }


    private fun logOut() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkPermissions() {
        permissionUtils = PermissionUtils()
        permissionLauncher = permissionUtils.showPermissionsRequest(this)
        if (!permissionUtils.hasPermission(this)) {
            permissionUtils.requestPermissions(permissionLauncher)
        }
    }

    private fun openChart() {
        val intent = Intent(
            this@MainActivity,
            ChartActivity::class.java
        )
        startActivity(intent)
    }

    override fun onResume() {
        viewModel.onEvent(MainEvent.LoadMore())
        super.onResume()
    }
}