package com.example.bintagram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.FollowRvBinding
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FollowAdapter(var context: Context, var followList: ArrayList<User>): RecyclerView.Adapter<FollowAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: FollowRvBinding): RecyclerView.ViewHolder(binding.root)
    private lateinit var mDbRef: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FollowRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return followList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        mDbRef = FirebaseDatabase.getInstance().getReference()

        mDbRef.child(USER_NODE).child(followList.get(position).uid!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)!!
                Glide.with(context).load(user.image).placeholder(R.drawable.avatarr).into(holder.binding.profileImage)
                holder.binding.name.text = user.name
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }
}