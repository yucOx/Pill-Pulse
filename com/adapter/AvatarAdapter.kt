package com.yucox.pillpulse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.yucox.pillpulse.R

class AvatarAdapter(var context : Context, var avatarPhotos: ArrayList<Int>) :
    RecyclerView.Adapter<AvatarAdapter.ViewHolder>() {
    var database = FirebaseDatabase.getInstance()
    var selectedImage = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view  = LayoutInflater.from(parent.context).inflate(R.layout.select_avatar_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return avatarPhotos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val avatarPhoto = avatarPhotos[position]
        holder.avatarPfp.setImageResource(avatarPhoto)

        holder.avatarPfp.setOnClickListener {

        }
    }
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        var avatarPfp = view.findViewById<ImageView>(R.id.avatarIv)
    }
}