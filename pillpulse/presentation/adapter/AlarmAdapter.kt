package com.yucox.pillpulse.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.ReminderItemv2Binding
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.util.gone
import com.yucox.pillpulse.util.isGone
import com.yucox.pillpulse.util.showToastLong
import com.yucox.pillpulse.util.toDate
import com.yucox.pillpulse.util.visible

class AlarmAdapter(
    private val context: Context,
    private val alarmList: MutableList<AlarmRealm>,
    private val deleteAlarm: (alarm: AlarmRealm) -> Unit,
    private val closeAlarm: (alarm: AlarmRealm) -> Unit,
    private val openAlarm: (alarm: AlarmRealm) -> Unit
) :
    RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ReminderItemv2Binding.inflate(inflater, parent, false)
        return (ViewHolder(binding))
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.deleteItemBtn.visibility = View.GONE
        val sortedAlarmInfos = alarmList.sortedByDescending { it.alarmTime.toDate() }
        val alarm = sortedAlarmInfos[position]

        holder.binding.time.text = alarm.alarmTime
        holder.binding.pillNameItemTv.text = alarm.pillName
        holder.binding.pillInfoItemTv.text = alarm.info

        if (alarm.onOrOff.equals(0)) {
            holder.binding.checkBox.setImageResource(R.drawable.checkboxoff_40x20)
        } else {
            holder.binding.checkBox.setImageResource(R.drawable.checkbox_on40x20)
        }

        holder.binding.checkBox.setOnClickListener {
            if (alarm.onOrOff == 0) {
                holder.binding.checkBox.setImageResource(R.drawable.checkbox_on40x20)
                openAlarm(alarm)
            } else {
                holder.binding.checkBox.setImageResource(R.drawable.checkboxoff_40x20)
                closeAlarm(alarm)
            }
        }
        holder.binding.frameItemConst.setOnClickListener {
            if (holder.binding.deleteItemBtn.isGone())
                context.showToastLong("Düzenlemek için basılı tut")
        }

        holder.binding.frameItemConst.setOnLongClickListener {
            if (holder.binding.deleteItemBtn.isGone()) {
                holder.binding.deleteItemBtn.visible()

            } else {
                holder.binding.deleteItemBtn.gone()
            }
            true
        }
        holder.binding.deleteItemBtn.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle("Silmek istediğinze emin misiniz?")
                .setMessage("Bu işlem geri alınamaz")
                .setNegativeButton("Evet") { dialog, which ->
                    deleteAlarm(alarm)
                }
                .setPositiveButton("Hayır") { dialog, which -> }
                .show()
        }
    }

    class ViewHolder(val binding: ReminderItemv2Binding) : RecyclerView.ViewHolder(binding.root) {
    }

}