package com.example.bintagram.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.Message
import com.example.bintagram.activity.MessageActivity
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.MessageUserBinding
import com.example.bintagram.utils.CHAT
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

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

        lateinit var mDbRef: DatabaseReference
        mDbRef = FirebaseDatabase.getInstance().getReference()
        var receiverRoom: String? = null
        var senderRoom: String? = null

        Glide.with(context).load(userMessageList.get(position).image).placeholder(R.drawable.avatarr).into(holder.binding.profileImage)
        holder.binding.name.text = userMessageList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("uid", userMessageList.get(position).uid)

            context.startActivity(intent)
        }


        val receiverUid = userMessageList.get(position).uid
        val senderUid = Firebase.auth.currentUser!!.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        mDbRef.child(CHAT).child(senderRoom).child("messages").orderByKey().limitToLast(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)!!
                        holder.binding.message.text = message.message
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }
}