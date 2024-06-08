package com.example.bintagram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.Comment
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.CommentRvBinding
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class CommentAdapter(var context: Context, var commentList:ArrayList<Comment>): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    inner class ViewHolder(var binding: CommentRvBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = CommentRvBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var commentorUid = commentList.get(position).commentorId

        val mDbRef : DatabaseReference

        mDbRef = FirebaseDatabase.getInstance().getReference()

        mDbRef.child(USER_NODE).child(commentorUid!!).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)!!
            if (user.image.isNullOrEmpty()){

            }
            else{
                Glide.with(context).load(user.image).placeholder(R.drawable.user).into(holder.binding.profileImage)
                holder.binding.name.text = user.name
            }
        }


        holder.binding.comment.text = commentList.get(position).comment

    }
}