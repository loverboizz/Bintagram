package com.example.bintagram.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.Post
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.PostRvBinding
import com.example.bintagram.utils.FOLLOW
import com.example.bintagram.utils.LIKE
import com.example.bintagram.utils.USER_NODE
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class PostAdapter(var context: Context,var postList: ArrayList<Post>): RecyclerView.Adapter<PostAdapter.MyHolder>() {
    inner class MyHolder(var binding: PostRvBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        var binding=PostRvBinding.inflate(LayoutInflater.from(context),parent,false)

        return MyHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        var isLike = false
        try {
            Firebase.firestore.collection(USER_NODE).document(postList.get(position).uid).get().addOnSuccessListener {
                var user=it.toObject<User>()!!
                Glide.with(context).load(user!!.image).placeholder(R.drawable.user).into(holder.binding.profileImage)
                holder.binding.name.text= user.name
            }
        }catch (e: Exception){

        }

        Glide.with(context).load(postList.get(position).postUrl).placeholder(R.drawable.loading).into(holder.binding.postImage)
        try {
            val text = TimeAgo.using(postList.get(position).time.toLong())
            holder.binding.time.text=text
        }catch (e : Exception){
            holder.binding.time.text=""
        }

        holder.binding.share.setOnClickListener{
            var i = Intent(android.content.Intent.ACTION_SEND)
            i.type="text/plain"
            i.putExtra(Intent.EXTRA_TEXT, postList.get(position).postUrl)
            context.startActivity(i)
        }
        holder.binding.caption.text=postList.get(position).caption

        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ LIKE)
            .whereEqualTo("postUrl",postList.get(position).postUrl).get().addOnSuccessListener {

                if (it.documents.size==0){
                    isLike=false
                }else{
                    holder.binding.like.setImageResource(R.drawable.heart)
                    isLike=true
                }
            }
        holder.binding.like.setOnClickListener{
            if (isLike){

                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ LIKE)
                    .whereEqualTo("postUrl",postList.get(position).postUrl).get().addOnSuccessListener {
                        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ LIKE).document(it.documents.get(0).id).delete()
                        holder.binding.like.setImageResource(R.drawable.redheart)
                        isLike=false
                    }
            }else{
                Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ LIKE).document()
                    .set(postList.get(position))
                holder.binding.like.setImageResource(R.drawable.heart)
                isLike= true
            }

        }

    }


}