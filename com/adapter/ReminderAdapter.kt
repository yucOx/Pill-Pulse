package com.yucox.pillpulse.adapter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.ReminderItemBinding
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.model.BroadcastReceiver
import java.util.Calendar

class ReminderAdapter(var context: Context, var alarmInfos : ArrayList<AlarmInfo>) :
    RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {
    var calendar = Calendar.getInstance()
    var auth = FirebaseAuth.getInstance()
    var database = FirebaseDatabase.getInstance().getReference("Alarms")
    private lateinit var alarmManager: AlarmManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var inflater = LayoutInflater.from(parent.context)
        val binding = ReminderItemBinding.inflate(inflater,parent,false)
        return(ViewHolder(binding))
    }
    override fun getItemCount(): Int {
        return alarmInfos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.deleteItemBtn.visibility = View.GONE
        var sortedAlarmInfos = alarmInfos.sortedBy { it.alarmTime.hours}
        val alarmInfo = sortedAlarmInfos[position]
        calendar.time = alarmInfo.alarmTime
        holder.binding.time.setText(calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + calendar.get(Calendar.MINUTE))
        holder.binding.pillNameItemTv.setText(alarmInfo.pillName)
        holder.binding.pillInfoItemTv.setText(alarmInfo.info)

        if(alarmInfo.onOrOff.equals(0)){
            holder.binding.openorclosetext.setText("Kapalı")
            holder.binding.checkBox.setImageResource(R.drawable.checkboxoff_40x20)
        }else{
            holder.binding.openorclosetext.setText("Açık")
            holder.binding.checkBox.setImageResource(R.drawable.checkbox_on40x20)
        }
        if(alarmInfo.repeating == 0){
            holder.binding.repeatItemBtn.setImageResource(R.drawable.repeatnormal)
        }
        else if(alarmInfo.repeating == 1 && alarmInfo.onOrOff == 1){
            holder.binding.repeatItemBtn.setImageResource(R.drawable.repeatfocus)
        }

        holder.binding.repeatItemBtn.setOnClickListener {
            var ref = database.child(alarmInfo.alarmLocation.toString()).child("repeating")
            if(alarmInfo.repeating == 0){
                alarmInfo.repeating = 1
                holder.binding.repeatItemBtn.setImageResource(R.drawable.repeatfocus)
                ref.setValue(1)
            }else{
                alarmInfo.repeating = 0
                holder.binding.repeatItemBtn.setImageResource(R.drawable.repeatnormal)
                ref.setValue(0)
            }
        }

        holder.binding.checkBox.setOnClickListener {
            if(alarmInfo.onOrOff == 0) {
                holder.binding.openorclosetext.setText("Açık")
                holder.binding.repeatItemBtn.setImageResource(R.drawable.repeatfocus)
                holder.binding.checkBox.setImageResource(R.drawable.checkbox_on40x20)
                openTheAlarm(alarmInfo)
            }else{
                holder.binding.openorclosetext.setText("Kapalı")
                holder.binding.repeatItemBtn.setImageResource(R.drawable.repeatnormal)
                holder.binding.checkBox.setImageResource(R.drawable.checkboxoff_40x20)
                closeTheAlarm(alarmInfo)
            }
        }
        holder.binding.frameItemConst.setOnClickListener {
            if(holder.binding.deleteItemBtn.visibility == View.GONE)
                Toast.makeText(context,"Düzenlemek için basılı tut",Toast.LENGTH_LONG).show()
        }

        holder.binding.frameItemConst.setOnLongClickListener(object : View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                if(holder.binding.deleteItemBtn.visibility == View.GONE){
                    holder.binding.deleteItemBtn.visibility = View.VISIBLE
                }else{
                    holder.binding.deleteItemBtn.visibility = View.GONE
                }
                return true
            }
        })
        holder.binding.deleteItemBtn.setOnClickListener {
            var builder = MaterialAlertDialogBuilder(context)
            builder.setTitle("Silmek istediğinze emin misiniz?")
                .setMessage("Bu işlem geri alınamaz")
                .setNegativeButton("Evet"){
                    dialog,which ->
                    var ref = database.child(alarmInfo.alarmLocation.toString()).removeValue()
                        .addOnSuccessListener {
                        deleteAndCloseAlarm(alarmInfo)
                        alarmInfos.remove(alarmInfo)
                        notifyDataSetChanged()
                    }
                }
                .setPositiveButton("Hayır"){dialog,which ->}
                .show()
        }
    }

    private fun deleteAndCloseAlarm(alarmInfo: AlarmInfo){
        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context.applicationContext, BroadcastReceiver::class.java)
        intent.putExtra("alarmInfo",alarmInfo)
        var pendingIntent = PendingIntent.getBroadcast(context.applicationContext,alarmInfo.requestCode,intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun closeTheAlarm(alarmInfo: AlarmInfo) {
        var ref = database.child(alarmInfo.alarmLocation.toString()).child("onOrOff")
        ref.setValue(0)
        alarmInfo.onOrOff = 0
        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context.applicationContext, BroadcastReceiver::class.java)
        intent.putExtra("alarmInfo",alarmInfo)
        var pendingIntent = PendingIntent.getBroadcast(context.applicationContext,alarmInfo.requestCode,intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun openTheAlarm(alarmInfo : AlarmInfo) {
        var ref = database.child(alarmInfo.alarmLocation.toString()).child("onOrOff")
        ref.setValue(1)
        alarmInfo.onOrOff = 1
        var calendar2 = Calendar.getInstance()
        calendar2.set(Calendar.HOUR_OF_DAY,alarmInfo.alarmTime.hours)
        calendar2.set(Calendar.MINUTE,alarmInfo.alarmTime.minutes)
        if (calendar2.timeInMillis <= System.currentTimeMillis()) {
            calendar2.add(Calendar.DAY_OF_YEAR, 1)
        }
        println(alarmInfo.alarmTime)
        println(calendar2.get(Calendar.HOUR))
        println(calendar2.get(Calendar.MINUTE))
        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context.applicationContext, BroadcastReceiver::class.java)
        intent.putExtra("alarmInfo",alarmInfo)
        var pendingIntent = PendingIntent.getBroadcast(context.applicationContext,alarmInfo.requestCode,intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar2.timeInMillis, AlarmManager.INTERVAL_DAY,pendingIntent)
        Toast.makeText(context,"Hatırlatıcı aktif edildi", Toast.LENGTH_SHORT).show()
    }

    class ViewHolder(val binding : ReminderItemBinding) : RecyclerView.ViewHolder(binding.root) {
    }

}