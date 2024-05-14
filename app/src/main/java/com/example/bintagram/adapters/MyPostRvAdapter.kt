package com.example.bintagram.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bintagram.Models.Post
import com.example.bintagram.activity.ViewPostActivity
import com.example.bintagram.databinding.MyPostRvDesignBinding
import com.squareup.picasso.Picasso

class MyPostRvAdapter (var context: Context, var postList:ArrayList<Post>): RecyclerView.Adapter<MyPostRvAdapter.ViewHolder>(){



    inner class ViewHolder(var binding: MyPostRvDesignBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var binding = MyPostRvDesignBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(postList.get(position).postUrl).into(holder.binding.postImage)
        holder.binding.postImage.setOnClickListener {

            val intent = Intent(context, ViewPostActivity::class.java)
            intent.putExtra("lastPosition",  holder.adapterPosition.toString())
            context.startActivity(intent)
        }

    }



}       