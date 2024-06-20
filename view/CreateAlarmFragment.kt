package com.yucox.pillpulse.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.yucox.pillpulse.R
import com.yucox.pillpulse.viewmodel.AlarmViewModel
import com.yucox.pillpulse.databinding.FragmentCreateAlarmBinding
import com.yucox.pillpulse.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
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
        binding = FragmentCreateAlarmBinding.inflate(layoutInflater)

        viewModel.message.observe(requireActivity()) {
            if (it.isNotEmpty()) {
                Toast.makeText(
                    requireActivity(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.setAlarmBtn.setOnClickListener {
            setAlarm()
        }

        binding.selectTimeBtn.setOnClickListener {
            showTimePicker()
        }
        return binding.root

    }

    private fun setAlarm() {
        if (binding.pillNameEt.text.toString().isBlank()) {
            Toast.makeText(
                requireActivity(),
                "İlaç ismini giriniz",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.initAlarmObjects(
            binding.pillNameEt.text.toString(),
            1
        )
        viewModel.setAlarm(
            requireActivity()
        )
        requireActivity().findViewById<RecyclerView>(R.id.listAlarmRv)?.visibility =
            View.VISIBLE
        parentFragmentManager.popBackStack()
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
            val formattedTime = TimeUtils.toStringClock(tempCalendar.time)
            Toast.makeText(
                requireActivity(),
                "$formattedTime Seçildi",
                Toast.LENGTH_LONG
            ).show()
        }
        viewModel.calendar = tempCalendar
    }

    override fun onDestroy() {
        viewModel.viewModelScope.cancel()
        super.onDestroy()
    }
}