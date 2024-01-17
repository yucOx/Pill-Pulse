package com.yucox.pillpulse.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.R
import com.yucox.pillpulse.databinding.AddTimeActivityBinding
import com.yucox.pillpulse.model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class AddTimeActivity : AppCompatActivity() {
    private lateinit var binding: AddTimeActivityBinding
    private var database = FirebaseDatabase.getInstance()
    private var auth = FirebaseAuth.getInstance()
    lateinit var mAdView : AdView
    private var pillDetails = ArrayList<PillTime>()
    private var pastPills = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddTimeActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pillDetails = intent.getSerializableExtra("pillDetails") as ArrayList<PillTime>

        setListenerForInputs()
        save()

        backToPrevious()
        initBannerAd(binding.adView)
        selectFromPast()
    }

    private fun selectFromPast() {
        var tempPillsLower = ArrayList<String>()

        for(a in pillDetails){
            if(a.drugName.lowercase() in tempPillsLower){
                continue
            }else{
                pastPills.add(a.drugName.capitalize())
                tempPillsLower.add(a.drugName.lowercase())
            }
        }
        if(pastPills.isEmpty()){
            pastPills.add("Daha önce kaydettiğiniz bir ilaç bulunamadı.")
        }
        var arrayAdapter = ArrayAdapter<String>(this@AddTimeActivity,android.R.layout.simple_list_item_1,pastPills)
        binding.listView.adapter = arrayAdapter
        binding.listView.setOnItemClickListener(object : AdapterView.OnItemClickListener{
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                binding.pillNameText.setText(pastPills[p2])
                binding.listView.visibility = View.GONE
            }
        })
        binding.listView.visibility = View.GONE
        binding.selectPastConst.setOnClickListener {
            if(binding.listView.visibility == View.GONE){
                binding.listView.visibility = View.VISIBLE
            }else{
                binding.listView.visibility = View.GONE
            }
        }
    }

    private fun initBannerAd(adView: AdView) {
        MobileAds.initialize(this) {}
        mAdView = adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun save() {
        binding.saveBtn.setOnClickListener {
            saveData(
                binding.pillNameText.text.toString(),
                binding.noteEt.text.toString(),
                database,
                auth.currentUser?.email.toString()
            )
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveData(
        pillName: String,
        note: String,
        database: FirebaseDatabase,
        userMail: String
    ) {
        var getRealTime = Date()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        var formatedDate = dateFormat.format(getRealTime)

        var key = database.getReference("Pills").push().key.toString()

        if (!pillName.isNullOrBlank()) {
            if (note.isBlank()) {
                var pillTimeAndNote =
                    PillTime(pillName, "Not almadınız.", getRealTime, userMail, key)
                database.getReference("Pills").child(key).setValue(pillTimeAndNote)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            CoroutineScope(Dispatchers.Main).launch {
                                finish()
                            }
                        }
                    }
            } else {
                var pillTimeAndNote = PillTime(pillName, note, getRealTime, userMail, key)
                database.getReference("Pills").child(key).setValue(pillTimeAndNote)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            CoroutineScope(Dispatchers.Main).launch {
                                finish()
                            }
                        }
                    }
            }
        }
    }

    private fun setListenerForInputs() {
        binding.pillNameText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.pillNameText.setBackgroundResource(R.drawable.pillinputfocussed)
            }

            override fun afterTextChanged(p0: android.text.Editable?) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding.pillNameText.setBackgroundResource(R.drawable.pillinputafter)
                    if (p0.isNullOrBlank()) {
                        binding.pillNameText.setBackgroundResource(R.drawable.pillinputnormal)
                    }
                }
            }

        })
        binding.noteEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.noteEt.setBackgroundResource(R.drawable.noteinputfocussed)
            }

            override fun afterTextChanged(p0: Editable?) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding.noteEt.setBackgroundResource(R.drawable.noteinputafter)
                    if (p0.isNullOrBlank()) {
                        binding.noteEt.setBackgroundResource(R.drawable.noteinputnormal)
                    }
                }
            }

        })
    }
    private fun backToPrevious() {
        binding.backIv.setOnClickListener {
            finish()
        }
    }
}