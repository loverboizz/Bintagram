package com.example.bintagram.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.Comment
import com.example.bintagram.Models.Post
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.PostRvBinding
import com.example.bintagram.utils.COMMENT
import com.example.bintagram.utils.LIKE
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.SAVE
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


class PostAdapter(var context: Context, var postList: ArrayList<Post>): RecyclerView.Adapter<PostAdapter.MyHolder>() {
    inner class MyHolder(var binding: PostRvBinding):RecyclerView.ViewHolder(binding.root)

    private var lastClickedPosition: Int = RecyclerView.NO_POSITION
    private var likeList= ArrayList<User>()
    private lateinit var likeAdapter : SearchAdapter

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

            val htmlText ="<b>${user.name}</b> ${postList.get(position).caption}"
            if (postList.get(position).caption!=""){
                holder.binding.caption.text= Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
            }else{
                holder.binding.caption.visibility = View.GONE
            }
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
                    holder.binding.nLike.setTypeface(holder.binding.nLike.typeface, Typeface.BOLD)
                    if (likeCount.equals("0")){
                        holder.binding.nLike.visibility =View.GONE
                    }else if(likeCount.equals("1")){
                        holder.binding.nLike.visibility = View.VISIBLE
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

        fun getCommentCount(postId: String, holder: MyHolder) {
            mDbRef.child(COMMENT).child(postId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val commentCount = snapshot.childrenCount.toString()
                    if (commentCount.equals("0")){
                        holder.binding.nComment.visibility =View.GONE
                    }else if(commentCount.equals("1")){
                        holder.binding.nComment.text = "1 comment"
                    }else{
                        holder.binding.nComment.text = "See all $commentCount comments"// Update your UI element
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
        }

        fun likeBtn() {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.like_user)
            val likeRecyclerView: RecyclerView = dialog.findViewById(R.id.likeRecyclerView)
            val likeList = ArrayList<User>()
            val likeAdapter: SearchAdapter
            likeAdapter = SearchAdapter(context, likeList)
            likeRecyclerView.layoutManager= LinearLayoutManager(context)
            likeRecyclerView.adapter =likeAdapter

            mDbRef.child(LIKE).child(postList.get(position).postId).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    likeList.clear()
                    for(userSnapshot in snapshot.children){

                        val user= userSnapshot.getValue(User::class.java)!!
                        if(Firebase.auth.currentUser!!.uid != user.uid){
                            likeList.add(user)
                        }


                    }
                    likeAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            dialog.show()
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
            dialog.window?.setGravity(Gravity.BOTTOM)
        }




        fun commentBtn(id : Int) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.comment)
            val commentRecyclerView: RecyclerView = dialog.findViewById(R.id.commentRecyclerView)
            val commentList = ArrayList<Comment>()
            val commentAdapter: CommentAdapter
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
                    commentList.reverse()
                    commentAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            val commentText: EditText= dialog.findViewById(R.id.commentBox)
            val commentBtn: ImageView = dialog.findViewById(R.id.send_button)

            commentBtn.setOnClickListener {
                val commentObject = Comment(commentText.text.toString() ,Firebase.auth.currentUser!!.uid)
                mDbRef.child(COMMENT).child(postList.get(position).postId)
                    .push().setValue(commentObject).addOnSuccessListener {
                        getCommentCount(postList.get(position).postId, holder)
                        commentText.setText("")
                    }

            }

            dialog.show()
            if (id==0){
                dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1000)
            }else{
                dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
            dialog.window?.setGravity(Gravity.BOTTOM)
        }

        getCommentCount(postList.get(position).postId, holder)

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

            commentBtn(0)
        }
        holder.binding.nComment.setOnClickListener {
            commentBtn(1)
        }

        holder.binding.profileImage.setOnClickListener{
            if (userUid.equals(Firebase.auth.currentUser!!.uid)){
            }else{
                val dialogFragment = ViewProfile.newInstance(userUid!!)
                dialogFragment.show((context as AppCompatActivity).supportFragmentManager, "ViewProfile")
            }
        }

        holder.binding.nLike.setOnClickListener {
            likeBtn()
        }


        mDbRef.child(SAVE).child(Firebase.auth.currentUser!!.uid).child(postList.get(position).postId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isLiked = snapshot.exists()
                    if (isLiked){

                        holder.binding.save.setImageResource(R.drawable.bookmark)

                    }else{
                        holder.binding.save.setImageResource(R.drawable.save)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        holder.binding.save.setOnClickListener {
            mDbRef.child(SAVE).child(Firebase.auth.currentUser!!.uid).child(postList.get(position).postId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isLike = snapshot.exists()
                        if (isLike){
                            snapshot.ref.removeValue()
                            holder.binding.save.setImageResource(R.drawable.save)
                        }else{
                            mDbRef.child(POST).child(postList.get(position).postId).get().addOnSuccessListener {
                                val post = it.getValue(Post::class.java)!!
                                snapshot.ref.setValue(post)
                                holder.binding.save.setImageResource(R.drawable.bookmark)
                            }

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }
}