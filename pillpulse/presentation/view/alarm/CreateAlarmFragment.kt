package com.yucox.pillpulse.presentation.view.alarm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.yucox.pillpulse.R
import com.yucox.pillpulse.presentation.viewmodel.AlarmViewModel
import com.yucox.pillpulse.databinding.FragmentCreateAlarmBinding
import com.yucox.pillpulse.presentation.event.AlarmEvent
import com.yucox.pillpulse.util.showToast
import com.yucox.pillpulse.util.showToastLong
import com.yucox.pillpulse.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CreateAlarmFragment : Fragment() {
    private lateinit var binding: FragmentCreateAlarmBinding
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupUI()
        return binding.root
    }


    private fun setupUI() {
        binding = FragmentCreateAlarmBinding.inflate(layoutInflater)
        binding.setAlarmBtn.setOnClickListener {
            setAlarm()
        }

        binding.selectTimeBtn.setOnClickListener {
            showTimePicker()
        }
    }

    private fun setAlarm() {
        lifecycleScope.launch {
            viewModel.initAlarmObjects(
                binding.pillNameEt.text.toString(),
                1
            )
            viewModel.onEvent(AlarmEvent.SaveAndScheduleAlarm)
        }
    }

    private fun showTimePicker() {
        val tempCalendar = Calendar.getInstance()
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(tempCalendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(tempCalendar.get(Calendar.MINUTE))
            .setTitleText("İlacı içmeniz gerek saati giriniz.")
            .build()
        picker.show(childFragmentManager, "test")
        picker.addOnPositiveButtonClickListener {
            tempCalendar[Calendar.HOUR_OF_DAY] = picker.hour
            tempCalendar[Calendar.MINUTE] = picker.minute
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = sdf.format(tempCalendar.time)
            showToastLong("Seçilen saat: $formattedTime")
        }
        viewModel.onEvent(AlarmEvent.UpdateTime(tempCalendar))
    }
}