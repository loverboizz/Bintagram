package com.example.bintagram.adapters

import android.app.Dialog
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bintagram.Models.Comment
import com.example.bintagram.Models.Reel
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.databinding.ReelDgBinding
import com.example.bintagram.utils.COMMENT
import com.example.bintagram.utils.LIKE
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.REEL
import com.example.bintagram.utils.USER_NODE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
        var user: User

        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()


        mDbRef.child(USER_NODE).child(reelList.get(position).uid).get().addOnSuccessListener {
            user = it.getValue(User::class.java)!!
            Glide.with(context).load(user.image).placeholder(R.drawable.user).into(holder.binding.profileImage)
            holder.binding.name.text= user.name
        }

//        Firebase.firestore.collection(USER_NODE).document(reelList.get(position).uid).get().addOnSuccessListener {
//            var user=it.toObject<User>()!!
//            Glide.with(context).load(user!!.image).placeholder(R.drawable.user).into(holder.binding.profileImage)
//            holder.binding.name.text= user.name
//        }


        holder.binding.comment.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.comment)
            var commentRecyclerView: RecyclerView = dialog.findViewById(R.id.commentRecyclerView)
            var commentList = ArrayList<Comment>()
            var commentAdapter: CommentAdapter
            commentAdapter = CommentAdapter(context, commentList)
            commentRecyclerView.layoutManager= LinearLayoutManager(context)
            commentRecyclerView.adapter =commentAdapter

            mDbRef.child(COMMENT).child(reelList.get(position).reelId).addValueEventListener(object :
                ValueEventListener {
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

            val commentText: EditText = dialog.findViewById(R.id.commentBox)
            val commentBtn: ImageView = dialog.findViewById(R.id.send_button)

            commentBtn.setOnClickListener {
                val commentObject = Comment(commentText.text.toString() ,Firebase.auth.currentUser!!.uid)
                mDbRef.child(COMMENT).child(reelList.get(position).reelId)
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


        mDbRef.child(LIKE).child(reelList.get(position).reelId).child(Firebase.auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isLiked = snapshot.exists()
                    if (isLiked){

                        holder.binding.like.setImageResource(R.drawable.redheart)

                    }else{
                        holder.binding.like.setImageResource(R.drawable.heart)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        holder.binding.like.setOnClickListener {
            mDbRef.child(LIKE).child(reelList.get(position).reelId).child(Firebase.auth.currentUser!!.uid)
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
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }


        holder.binding.editMenu.setOnClickListener {
            if(reelList.get(position).uid== Firebase.auth.currentUser!!.uid) {


                val dialog = Dialog(context)
                dialog.setContentView(R.layout.crud_post)

                val editPost: LinearLayout = dialog.findViewById(R.id.edit_post)
                val deletePost: LinearLayout = dialog.findViewById(R.id.delete_post)

                editPost.setOnClickListener {
                    val dialogUd = Dialog(context)
                    dialogUd.setContentView(R.layout.update_reel)

                    val caption: TextInputLayout = dialogUd.findViewById(R.id.caption)
                    val cancel: Button = dialogUd.findViewById(R.id.cancel_button)
                    val update: Button = dialogUd.findViewById(R.id.update_button)

                    caption.editText!!.setText(reelList[position].caption)

                    cancel.setOnClickListener {
                        dialogUd.dismiss()
                        dialog.dismiss()
                    }

                    update.setOnClickListener {
                        val newCaption = caption.editText!!.text.toString()

                        val reel: Reel = Reel(reelList[position].reelId,
                            reelList[position].reelUrl,
                            newCaption,
                            reelList[position].uid)
                        mDbRef.child(REEL).child(reelList[position].reelId).setValue(reel)
                            .addOnSuccessListener {
                            dialogUd.dismiss()
                            dialog.dismiss()
                            Toast.makeText(
                                context,
                                "Update reel successfully!!",
                                Toast.LENGTH_LONG
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Delete reel fail. SomeThing went wrong !!",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                    dialogUd.show()
                    dialogUd.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    dialogUd.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialogUd.window?.attributes?.windowAnimations = R.style.DialoAnimation
                    dialogUd.window?.setGravity(Gravity.BOTTOM)
                    dialog.dismiss()


                }
                deletePost.setOnClickListener {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Delete the reel")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes") { dialog, which ->
                            mDbRef.child(POST).child(reelList.get(position).reelId).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Delete reel successfully!!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    dialog.dismiss()
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

        mDbRef.child(REEL).child(reelList[position].reelId).get().addOnSuccessListener {
            val reel = it.getValue(Reel::class.java)!!
            holder.binding.caption.text = reel.caption
        }
        holder.binding.videoView.setVideoPath(reelList.get(position).reelUrl)
        holder.binding.videoView.setOnPreparedListener{
            holder.binding.progressBar.visibility=View.GONE
            holder.binding.videoView.start()
        }
    }
}