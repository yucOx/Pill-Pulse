package com.yucox.pillpulse.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.adapter.ReminderAdapter
import com.yucox.pillpulse.databinding.AddReminderActivityBinding
import com.yucox.pillpulse.model.AlarmInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding : AddReminderActivityBinding
    private var database = FirebaseDatabase.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private var ref = database.getReference("Alarms")
    private var alarmInfos = ArrayList<AlarmInfo>()
    private lateinit var listAlarmAdapter : ReminderAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddReminderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        getData()
    }

    private fun setAdapter() {
        listAlarmAdapter = ReminderAdapter(this@AddReminderActivity,alarmInfos)
        binding.listAlarmRv.layoutManager = LinearLayoutManager(this@AddReminderActivity,RecyclerView.VERTICAL,false)
        binding.listAlarmRv.adapter = listAlarmAdapter
    }

    private fun getData() {
        alarmInfos.clear()
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.child("userMail").getValue()?.equals(auth.currentUser?.email) == true){
                            alarmInfos.add(snap.getValue(AlarmInfo::class.java)!!)
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    setAdapter()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setListeners() {
        binding.backIv.setOnClickListener {
            finish()
        }
        binding.goToCreateAlarmBtn.setOnClickListener {
            goToCreateAlarm()
        }
    }

    private fun goToCreateAlarm() {
        val intent = Intent(this@AddReminderActivity,CreateAlarm::class.java)
        startActivity(intent)
    }

    override fun onRestart() {
        getData()
        super.onRestart()
    }


}