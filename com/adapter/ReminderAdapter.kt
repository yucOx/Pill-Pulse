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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
        val alarmInfo = alarmInfos[position]
        calendar.time = alarmInfo.alarmTime
        holder.binding.time.setText(calendar.get(Calendar.HOUR).toString() + ":" + calendar.get(Calendar.MINUTE))
        holder.binding.pillNameItemTv.setText(alarmInfo.pillName)
        holder.binding.pillInfoItemTv.setText(alarmInfo.info)
        if(alarmInfo.repeating == 0){
        }else{
            openTheAlarm(alarmInfo)
        }
        if(alarmInfo.onOrOff.equals(0)){
            holder.binding.checkBox.setText("Kapalı")
        }else{
            holder.binding.checkBox.setText("Açık")
            holder.binding.checkBox.isChecked = true
        }
        holder.binding.checkBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                if(p1 == true){
                    openTheAlarm(alarmInfo)
                    holder.binding.checkBox.setText("Açık")
                }else{
                    closeTheAlarm(alarmInfo)
                    holder.binding.checkBox.setText("Kapalı")
                }
            }
        })

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
            var builder = AlertDialog.Builder(context)
            builder.setTitle("Silmek istediğinze emin misiniz?")
                .setMessage("Bu işlem geri alınamaz")
                .setNegativeButton("Evet"){
                    dialog,which ->
                    var ref = database.child(alarmInfo.alarmLocation.toString())
                    ref.removeValue().addOnSuccessListener {
                        closeTheAlarm(alarmInfo)
                        alarmInfos.remove(alarmInfo)
                        notifyDataSetChanged()
                    }
                }
                .setPositiveButton("Hayır"){dialog,which ->}
                .show()
        }
    }

    private fun closeTheAlarm(alarmInfo: AlarmInfo) {
        var ref = database.child(alarmInfo.alarmLocation.toString()).child("onOrOff")
        ref.setValue(0)
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
        var calendar2 = Calendar.getInstance()
        if (calendar2.timeInMillis <= System.currentTimeMillis()) {
            // Zaman geçmişse, bir sonraki günü belirleyin
            calendar2.add(Calendar.DAY_OF_YEAR, 1)
        }
        calendar2.set(Calendar.HOUR_OF_DAY,alarmInfo.alarmTime.hours)
        calendar2.set(Calendar.MINUTE,alarmInfo.alarmTime.minutes)
        println(alarmInfo.alarmTime)
        println(calendar2.get(Calendar.HOUR))
        println(calendar2.get(Calendar.MINUTE))
        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context.applicationContext, BroadcastReceiver::class.java)
        intent.putExtra("alarmInfo",alarmInfo)
        var pendingIntent = PendingIntent.getBroadcast(context.applicationContext,alarmInfo.requestCode,intent,
            PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar2.timeInMillis, AlarmManager.INTERVAL_DAY,pendingIntent)
        Toast.makeText(context,"Hatırlatıcı oluşturuldu", Toast.LENGTH_SHORT).show()
    }

    class ViewHolder(val binding : ReminderItemBinding) : RecyclerView.ViewHolder(binding.root) {
    }

}