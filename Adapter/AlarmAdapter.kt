package com.yucox.pillpulse.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.pillpulse.R
import com.yucox.pillpulse.Repository.AlarmRepository
import com.yucox.pillpulse.databinding.ReminderItemv2Binding
import com.yucox.pillpulse.Model.AlarmInfo
import com.yucox.pillpulse.AlarmUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class AlarmAdapter(
    private val context: Context,
    private val alarmInfos: ArrayList<AlarmInfo>
) :
    RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {
    private val _alarmRepository = AlarmRepository()
    private val _sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ReminderItemv2Binding.inflate(inflater, parent, false)
        return (ViewHolder(binding))
    }

    override fun getItemCount(): Int {
        return alarmInfos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.deleteItemBtn.visibility = View.GONE
        val sortedAlarmInfos = alarmInfos.sortedBy {
            it.alarmTime.hours
        }
        val alarmInfo = sortedAlarmInfos[position]
        val formattedTime = _sdf.format(alarmInfo.alarmTime)
        holder.binding.time.text = formattedTime
        holder.binding.pillNameItemTv.text = alarmInfo.pillName
        holder.binding.pillInfoItemTv.text = alarmInfo.info

        if (alarmInfo.onOrOff.equals(0)) {
            holder.binding.checkBox.setImageResource(R.drawable.checkboxoff_40x20)
        } else {
            holder.binding.checkBox.setImageResource(R.drawable.checkbox_on40x20)
        }

        holder.binding.checkBox.setOnClickListener {
            if (alarmInfo.onOrOff == 0) {
                holder.binding.checkBox.setImageResource(R.drawable.checkbox_on40x20)
                openTheAlarm(alarmInfo)
            } else {
                holder.binding.checkBox.setImageResource(R.drawable.checkboxoff_40x20)
                closeTheAlarm(alarmInfo)
            }
        }
        holder.binding.frameItemConst.setOnClickListener {
            if (holder.binding.deleteItemBtn.visibility == View.GONE)
                Toast.makeText(
                    context,
                    "Düzenlemek için basılı tut",
                    Toast.LENGTH_LONG
                ).show()
        }

        holder.binding.frameItemConst.setOnLongClickListener {
            if (holder.binding.deleteItemBtn.visibility == View.GONE) {
                holder.binding.deleteItemBtn.visibility = View.VISIBLE
            } else {
                holder.binding.deleteItemBtn.visibility = View.GONE
            }
            true
        }
        holder.binding.deleteItemBtn.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle("Silmek istediğinze emin misiniz?")
                .setMessage("Bu işlem geri alınamaz")
                .setNegativeButton("Evet") { dialog, which ->
                    deleteAndCloseAlarm(alarmInfo, position)
                }
                .setPositiveButton("Hayır") { dialog, which -> }
                .show()
        }
    }

    private fun deleteAndCloseAlarm(alarmInfo: AlarmInfo, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = _alarmRepository.deleteAlarmFromDatabase(
                alarmInfo.alarmLocation.toString()
            )

            withContext(Dispatchers.Main) {
                if (result.await()) {
                    AlarmUtils(context).deleteAndClose(alarmInfo)
                    alarmInfos.remove(alarmInfo)
                    notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        context,
                        "Lütfen daha sonra tekrar deneyiniz",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun closeTheAlarm(alarmInfo: AlarmInfo) {
        alarmInfo.onOrOff = 0
        _alarmRepository.saveAsClosed(alarmInfo.alarmLocation.toString())
        AlarmUtils(context).closeTheAlarm(alarmInfo)
    }

    private fun openTheAlarm(alarmInfo: AlarmInfo) {
        alarmInfo.onOrOff = 1
        _alarmRepository.saveAsOpen(alarmInfo.alarmLocation.toString())
        AlarmUtils(context).openTheAlarm(alarmInfo)

    }

    class ViewHolder(val binding: ReminderItemv2Binding) : RecyclerView.ViewHolder(binding.root) {
    }

}