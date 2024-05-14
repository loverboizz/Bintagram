package com.example.bintagram.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.activity.MessageActivity
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.MessageUserBinding

class MessageUserAdapter(var context: Context,var userMessageList: ArrayList<User>) : RecyclerView.Adapter<MessageUserAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: MessageUserBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = MessageUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userMessageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(userMessageList.get(position).image).placeholder(R.drawable.avatarr).into(holder.binding.profileImage)
        holder.binding.name.text = userMessageList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("uid", userMessageList.get(position).uid)

            context.startActivity(intent)
        }
    }
}