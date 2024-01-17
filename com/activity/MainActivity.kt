package com.yucox.pillpulse.activity

import ListAdapter
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yucox.pillpulse.R
import com.yucox.pillpulse.adapter.AvatarAdapter
import com.yucox.pillpulse.databinding.ActivityMainBinding
import com.yucox.pillpulse.model.OpenAlarmOnRestart
import com.yucox.pillpulse.model.PillTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var pillDetails = ArrayList<PillTime>()
    private var database = FirebaseDatabase.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private var reference = database.getReference("Pills")

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            }
        }

    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Bildirim izni")
            .setMessage("Hatırlatıcılar için bildirim izni gerekiyor, lütfen izin ver butonuna bas")
            .setPositiveButton("İzin ver") { _, _ ->
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Reddet", null)
            .show()
    }

    private fun showNotificationPermissionRationale() {

        MaterialAlertDialogBuilder(this, com.google.android.material.R.style.MaterialAlertDialog_Material3)
            .setTitle("Bildirim izni")
            .setMessage("Hatırlatıcılar için bildirim izni gerekiyor, lütfen izin ver butonuna bas")
            .setPositiveButton("İzin ver") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Reddet", null)
            .show()
    }

    var hasNotificationPermissionGranted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //binding.avatarRecyclerView.visibility = View.GONE

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermissionGranted = true
        }
        setWelcomeMessage()
        var avatarPhotos = ArrayList<Int>()
        //setAvatarPhotos(avatarPhotos)
        //selectAvatar()
        getData(reference,pillDetails,auth.currentUser?.email.toString())

        binding.addRemindenBtn.setOnClickListener {
            val intent = Intent(this@MainActivity,AddReminderActivity::class.java)
            startActivity(intent)
        }
        binding.addTimeBtn.setOnClickListener {
            val intent = Intent(this, AddTimeActivity::class.java)
            intent.putExtra("pillDetails",pillDetails)
            startActivity(intent)
        }
        binding.logoutBtn.setOnClickListener {
            logOut()
        }
    }

    private fun logOut() {
        auth.signOut()
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setWelcomeMessage() {
        database.getReference("UserInfo").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.child("mail").getValue()?.equals(auth.currentUser?.email) == true){
                            var user = snap.getValue(com.yucox.pillpulse.model.UserInfo::class.java)
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.welcomeTv.text = "Hoş geldin\n${user?.name + " " + user?.surname}"
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /*private fun selectAvatar() {
        binding.avatarPhoto.setOnClickListener {
            if(binding.avatarRecyclerView.visibility == View.VISIBLE){
                binding.avatarRecyclerView.visibility = View.GONE
            }else{
                binding.avatarRecyclerView.visibility = View.VISIBLE
            }
        }
    }*/
    /*
    private fun initSelectAvatarRecycler(
        avatarPhotos: ArrayList<Int>,
        avatarRecyclerView: RecyclerView
    ) {
        var adapter = AvatarAdapter(this@MainActivity,avatarPhotos)
        avatarRecyclerView.adapter = adapter
        avatarRecyclerView.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
    }*/

    private fun initRecycler(listPillsRecycler: RecyclerView, pillDetails: ArrayList<PillTime>) {
        var adapter = ListAdapter(this,pillDetails,reference)
        listPillsRecycler.adapter = adapter
        listPillsRecycler.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.progressBar2.visibility = View.GONE
    }

    private fun getData(
        reference: DatabaseReference,
        pillDetails: ArrayList<PillTime>,
        mail: String
    ) {
        pillDetails.clear()
        reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        if(snap.child("userMail").getValue()?.equals(mail) == true){
                            pillDetails.add(snap.getValue(PillTime::class.java)!!)
                        }
                    }
                }else{
                    pillDetails.add(
                        PillTime("Selam!","Buraya ilacını aldığın saati ve notlarını kaydedebilirsin", Date(),auth.currentUser?.email.toString(),""
                        ))
                }
                initRecycler(binding.listPillsRecycler,pillDetails)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun setAvatarPhotos(avatarPhotos: ArrayList<Int>) {
        avatarPhotos.add(R.drawable.avatar1)
        avatarPhotos.add(R.drawable.avatar2)
        avatarPhotos.add(R.drawable.avatar3)
        avatarPhotos.add(R.drawable.avatar4)
        avatarPhotos.add(R.drawable.avatar5)
        avatarPhotos.add(R.drawable.avatar6)
        avatarPhotos.add(R.drawable.avatar7)
        avatarPhotos.add(R.drawable.avatar8)
        avatarPhotos.add(R.drawable.avatar9)
        avatarPhotos.add(R.drawable.avatar10)
        avatarPhotos.add(R.drawable.avatar11)
        //initSelectAvatarRecycler(avatarPhotos,binding.avatarRecyclerView)
    }

    override fun onRestart() {
        binding.progressBar2.visibility = View.VISIBLE
        getData(reference,pillDetails,auth.currentUser?.email.toString())
        super.onRestart()
    }
}