package com.yucox.pillpulse.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.R
import com.yucox.pillpulse.adapter.ReminderAdapter
import com.yucox.pillpulse.databinding.AddReminderActivityBinding
import com.yucox.pillpulse.model.AlarmInfo
import com.yucox.pillpulse.model.OpenAlarmOnRestart
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
    lateinit var mAdView : AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddReminderActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        getData()
        initBannerAd()
        pleaseOpenAgainInRestart()
    }

    private fun pleaseOpenAgainInRestart() {
        var builder = MaterialAlertDialogBuilder(this@AddReminderActivity)
        builder.setTitle("Önemli!!")
            .setMessage("Lütfen cihazı açıp kapattığınızda hatırlatıcının düzgün çalışması için 'Hatırlatıcılar' sayfasını tekrar açın")
            .setNegativeButton("Anladım"){dialog,which ->}
            .show()
    }

    private fun initBannerAd() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun setAdapter() {
        listAlarmAdapter = ReminderAdapter(this@AddReminderActivity,alarmInfos)
        binding.listAlarmRv.layoutManager = LinearLayoutManager(this@AddReminderActivity,RecyclerView.VERTICAL,false)
        binding.listAlarmRv.adapter = listAlarmAdapter
    }

    private fun getData() {
        var openAlarmOnRestart = OpenAlarmOnRestart(this)
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
                    for(alarmInfo in alarmInfos){
                        if(alarmInfo.onOrOff == 1){
                            openAlarmOnRestart.openTheAlarm(alarmInfo)
                        }
                    }
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