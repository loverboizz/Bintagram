package com.example.bintagram.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.Comment
import com.example.bintagram.Models.Post
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.PostRvBinding
import com.example.bintagram.utils.COMMENT
import com.example.bintagram.utils.FOLLOW
import com.example.bintagram.utils.LIKE
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.USER_NODE
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView


class PostAdapter(var context: Context,var postList: ArrayList<Post>): RecyclerView.Adapter<PostAdapter.MyHolder>() {
    inner class MyHolder(var binding: PostRvBinding):RecyclerView.ViewHolder(binding.root)

    private var lastClickedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        var binding=PostRvBinding.inflate(LayoutInflater.from(context),parent,false)

        return MyHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    fun scrollToLastClickedPosition(recyclerView: RecyclerView) {
        val lastPosition = lastClickedPosition
        if (lastPosition != RecyclerView.NO_POSITION) {
            recyclerView.smoothScrollToPosition(lastPosition)
        }
    }

    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        lateinit var mDbRef: DatabaseReference
        mDbRef = FirebaseDatabase.getInstance().getReference()

        var userUid:String? = null
        mDbRef.child(USER_NODE).child(postList.get(position).uid).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)!!
            holder.binding.name.text = user.name ?: ""
            userUid = user.uid
            if (!user.image.isNullOrEmpty()) {
                Glide.with(context).load(user.image).placeholder(R.drawable.user).into(holder.binding.profileImage)
            }
        }


        Glide.with(context).load(postList.get(position).postUrl).placeholder(R.drawable.loading).into(holder.binding.postImage)
        try {
            val text = TimeAgo.using(postList.get(position).time.toLong())
            holder.binding.time.text=text
        }catch (e : Exception){
            holder.binding.time.text=""
        }
        holder.binding.caption.text=postList.get(position).caption

        holder.binding.share.setOnClickListener{
            var i = Intent(android.content.Intent.ACTION_SEND)
            i.type="text/plain"
            i.putExtra(Intent.EXTRA_TEXT, postList.get(position).postUrl)
            context.startActivity(i)
        }


        fun getLikeCount(postId: String, holder: MyHolder) {
            mDbRef.child(LIKE).child(postId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likeCount = snapshot.childrenCount.toString()
                    if (likeCount.equals("0")){
                        holder.binding.nLike.visibility =View.GONE
                    }else if(likeCount.equals("1")){
                        holder.binding.nLike.text = "1 like"
                    }else{
                        holder.binding.nLike.text = "$likeCount likes"// Update your UI element
                    }
                    
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
        }

        mDbRef.child(LIKE).child(postList.get(position).postId).child(Firebase.auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isLiked = snapshot.exists()
                    if (isLiked){

                        holder.binding.like.setImageResource(R.drawable.redheart)

                    }else{
                        holder.binding.like.setImageResource(R.drawable.heart)
                    }
                    getLikeCount(postList.get(position).postId, holder)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        holder.binding.like.setOnClickListener {

            mDbRef.child(LIKE).child(postList.get(position).postId).child(Firebase.auth.currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isLike = snapshot.exists()
                        if (isLike){
                            snapshot.ref.removeValue()
                            holder.binding.like.setImageResource(R.drawable.heart)
                        }else{
                            mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
                                val user = it.getValue(User::class.java)!!
                                snapshot.ref.setValue(user)
                                holder.binding.like.setImageResource(R.drawable.redheart)
                            }

                        }
                        getLikeCount(postList.get(position).postId, holder)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }



        holder.binding.editMenu.setOnClickListener{
            if(userUid== Firebase.auth.currentUser!!.uid) {


                val dialog = Dialog(context)
                dialog.setContentView(R.layout.crud_post)

                val editPost: LinearLayout = dialog.findViewById(R.id.edit_post)
                val deletePost: LinearLayout = dialog.findViewById(R.id.delete_post)

                editPost.setOnClickListener {
                    val dialogUpdate = Dialog(context)
                    dialogUpdate.setContentView(R.layout.update_post)

                    val cancel: Button = dialogUpdate.findViewById(R.id.cancel_button)
                    val image: RoundedImageView = dialogUpdate.findViewById(R.id.update_image)
                    val caption: TextInputLayout = dialogUpdate.findViewById(R.id.caption)
                    val updateBtn: Button = dialogUpdate.findViewById(R.id.update_button)
                    Glide.with(context).load(postList.get(position).postUrl)
                        .placeholder(R.drawable.loading).into(image)
                    caption.editText!!.setText(postList.get(position).caption)

                    cancel.setOnClickListener {
                        dialogUpdate.dismiss()
                        dialog.dismiss()
                    }

                    updateBtn.setOnClickListener {
                        val newCaption = caption.editText!!.text.toString()
                        val post: Post = Post(
                            postList.get(position).postId,
                            postList.get(position).postUrl,
                            newCaption,
                            postList.get(position).uid,
                            postList.get(position).time
                        )

                        mDbRef.child(POST).child(postList.get(position).postId).setValue(post)
                            .addOnSuccessListener {
                                dialogUpdate.dismiss()
                                dialog.dismiss()
                                Toast.makeText(
                                    context,
                                    "Update post successfully!!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Delete post fail. SomeThing went wrong !!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }




                    dialogUpdate.show()
                    dialogUpdate.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    dialogUpdate.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialogUpdate.window?.attributes?.windowAnimations = R.style.DialoAnimation
                    dialogUpdate.window?.setGravity(Gravity.BOTTOM)
                    dialog.dismiss()
                }
                deletePost.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Delete the post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes") { dialog, which ->
                            mDbRef.child(POST).child(postList.get(position).postId).removeValue()
                                .addOnSuccessListener {
                                    dialog.dismiss()
                                    Toast.makeText(
                                        context,
                                        "Delete post successfully!!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                }
                        }
                        .setNegativeButton("No") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                }
                dialog.show()
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
                dialog.window?.setGravity(Gravity.BOTTOM)
            }
        }


        holder.binding.comment.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.comment)
            var commentRecyclerView: RecyclerView = dialog.findViewById(R.id.commentRecyclerView)
            var commentList = ArrayList<Comment>()
            var commentAdapter: CommentAdapter
            commentAdapter = CommentAdapter(context, commentList)
            commentRecyclerView.layoutManager= LinearLayoutManager(context)
            commentRecyclerView.adapter =commentAdapter

            mDbRef.child(COMMENT).child(postList.get(position).postId).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentList.clear()
                    for(postSnapshot in snapshot.children){

                        val comment= postSnapshot.getValue(Comment::class.java)
                        commentList.add(comment!!)

                    }
                    commentAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            val commentText: EditText= dialog.findViewById(R.id.commentBox)
            val commentBtn: ImageView = dialog.findViewById(R.id.send_button)

            commentBtn.setOnClickListener {
                val commentObject = Comment(commentText.text.toString() ,userUid)
                mDbRef.child(COMMENT).child(postList.get(position).postId)
                    .push().setValue(commentObject).addOnSuccessListener {
                        commentText.setText("")
                    }

            }

            dialog.show()
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
            dialog.window?.setGravity(Gravity.BOTTOM)
        }







    }




}