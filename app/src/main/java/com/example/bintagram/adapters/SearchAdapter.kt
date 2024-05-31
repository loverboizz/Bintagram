package com.example.bintagram.adapters

import ViewProfile
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.SearchRvBinding
import com.example.bintagram.utils.FOLLOW
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class SearchAdapter (var context: Context, var userList: ArrayList<User>): RecyclerView.Adapter<SearchAdapter.ViewHolder>() {


    inner class ViewHolder(var binding: SearchRvBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SearchRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var mDbRef: DatabaseReference
        var user = userList[position]
        Glide.with(context).load(userList.get(position).image).placeholder(R.drawable.avatarr).into(holder.binding.profileImage)
        holder.binding.name.text = userList.get(position).name

        val currentUserUid = Firebase.auth.currentUser?.uid
        val followedUid = userList.get(position).uid!!
        mDbRef = FirebaseDatabase.getInstance().getReference()

        mDbRef.child(FOLLOW).child(currentUserUid!!).child(followedUid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
            @SuppressLint("ResourceAsColor")
            override fun onDataChange(snapshot: DataSnapshot) {
                val isFollowed = snapshot.exists()
                if (isFollowed){
                    holder.binding.follow.text = "Unfollow"
                    holder.binding.follow.setBackgroundColor(ContextCompat.getColor(context, R.color.grey))

                }else{
                    holder.binding.follow.text="Follow"
                    holder.binding.follow.setBackgroundColor(ContextCompat.getColor(context, R.color.blue))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        holder.binding.follow.setOnClickListener {
            mDbRef.child(FOLLOW).child(currentUserUid).child(followedUid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    @SuppressLint("ResourceAsColor")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isFollowed = snapshot.exists()
                        if (isFollowed){
                            snapshot.ref.removeValue()
                            holder.binding.follow.text = "Follow"
                            holder.binding.follow.setBackgroundColor(ContextCompat.getColor(context, R.color.blue))
                        }else{
                            snapshot.ref.setValue(user)
                            holder.binding.follow.text = "Unfollow"
                            holder.binding.follow.setBackgroundColor(ContextCompat.getColor(context, R.color.grey))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

        holder.binding.userItem.setOnClickListener{
            val dialogFragment = ViewProfile.newInstance(userList.get(position).uid!!)
            dialogFragment.show((context as AppCompatActivity).supportFragmentManager, "ViewProfile")
        }
    }
}

