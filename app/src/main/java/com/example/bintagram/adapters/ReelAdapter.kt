package com.example.bintagram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.Reel
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.ReelDgBinding
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class ReelAdapter(var context:Context, var reelList: ArrayList<Reel>) : RecyclerView.Adapter<ReelAdapter.ViewHolder>() {

    inner class ViewHolder(var binding:ReelDgBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = ReelDgBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reelList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Firebase.firestore.collection(USER_NODE).document(reelList.get(position).uid).get().addOnSuccessListener {
            var user=it.toObject<User>()!!
            Glide.with(context).load(user!!.image).placeholder(R.drawable.user).into(holder.binding.profileImage)
            holder.binding.name.text= user.name
        }
        holder.binding.caption.setText(reelList.get(position).caption)
        holder.binding.videoView.setVideoPath(reelList.get(position).reelUrl)
        holder.binding.videoView.setOnPreparedListener{
            holder.binding.progressBar.visibility=View.GONE
            holder.binding.videoView.start()
        }
    }
}