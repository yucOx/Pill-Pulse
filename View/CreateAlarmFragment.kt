package com.yucox.pillpulse.View

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.Model.AlarmInfo
import com.yucox.pillpulse.R
import com.yucox.pillpulse.ViewModel.AlarmViewModel
import com.yucox.pillpulse.databinding.FragmentCreateAlarmBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateAlarmFragment : Fragment() {
    private lateinit var binding: FragmentCreateAlarmBinding
    private lateinit var viewModel: AlarmViewModel

    private lateinit var calendar: Calendar
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fragmentManager: FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateAlarmBinding.inflate(layoutInflater)

        fragmentManager = parentFragmentManager

        calendar = Calendar.getInstance()

        viewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)

        binding.setAlarmBtn.setOnClickListener {
            setAlarm()
        }

        binding.selectTimeBtn.setOnClickListener {
            showTimePicker()
        }

        return binding.root
    }
    private fun setAlarm() {
        val key = FirebaseDatabase.getInstance()
            .getReference("Alarms")
            .push().key

        if (!binding.pillNameEt.text.toString().isBlank()) {
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                Toast.makeText(
                    requireActivity().applicationContext,
                    "Hatırlatıcı yarına ayarlandı",
                    Toast.LENGTH_LONG
                ).show()
            }

            val requestCode = System.currentTimeMillis().toInt()
            val alarm = AlarmInfo(
                requestCode,
                binding.pillNameEt.text.toString(),
                "",
                1,
                auth.currentUser?.email,
                key,
                calendar.time,
                1
            )

            CoroutineScope(Dispatchers.Main).launch {
                viewModel.updateAlarm(alarm)

                viewModel.setAlarm(
                    requireActivity(),
                    calendar
                )

                val result = viewModel.savePillAlarm(
                    key!!,
                    viewModel
                )

                if (result) {
                    Toast.makeText(
                        requireActivity(),
                        "Hatırlatıcı ayarlandı",
                        Toast.LENGTH_LONG
                    ).show()
                    activity?.findViewById<RecyclerView>(R.id.listAlarmRv)?.visibility = View.VISIBLE
                    fragmentManager.popBackStack()
                }
            }


        } else {
            Toast.makeText(
                requireActivity(),
                "İlaç ismini giriniz",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText("İlacı içmeniz gerek saati giriniz.")
            .build()
        picker.show(childFragmentManager, "test")
        picker.addOnPositiveButtonClickListener {
            calendar[Calendar.HOUR_OF_DAY] = picker.hour
            calendar[Calendar.MINUTE] = picker.minute
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = sdf.format(calendar.time)
            Toast.makeText(
                requireActivity(),
                "$formattedTime Seçildi",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}