package com.example.bintagram.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bintagram.Models.User
import com.example.bintagram.adapters.FollowAdapter
import com.example.bintagram.adapters.MessageUserAdapter
import com.example.bintagram.databinding.ActivityChatBinding
import com.example.bintagram.utils.CHAT
import com.example.bintagram.utils.FOLLOW
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class ChatActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private var followList=ArrayList<User>()
    private lateinit var followAdapter: FollowAdapter
    private var userMessageList = ArrayList<User>()
    private lateinit var userAdapter: MessageUserAdapter
    private lateinit var mDbRef : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        setSupportActionBar(binding.materialToolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(Intent(this@ChatActivity, HomeActivity::class.java))
            finish()
        }
        userAdapter = MessageUserAdapter(this, userMessageList)
        binding.userRv.layoutManager = LinearLayoutManager(this)
        binding.userRv.adapter = userAdapter





        followAdapter = FollowAdapter(this, followList)
        binding.folowRv.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.folowRv.adapter = followAdapter
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            if (user != null) {
                if (!user.image.isNullOrEmpty()) {
                    Picasso.get().load(user.image).into(binding.profileImage)
                }
            }
        }


        mDbRef.child(FOLLOW).child(Firebase.auth.currentUser!!.uid).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userMessageList.clear()
                for (i in snapshot.children){
                    val Followed = i.getValue(User::class.java)!!
                    Log.d("Follow Uid:", Followed.uid!!)

                    mDbRef.child(FOLLOW).child(Followed.uid!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(innersnapshot: DataSnapshot) {
                                for (j in innersnapshot.children){
                                    val currentUser = j.key
                                    if (currentUser.equals(Firebase.auth.currentUser!!.uid)){
                                        userMessageList.add(Followed)
                                    }
                                }
                                userAdapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })




        mDbRef.child(FOLLOW).child(Firebase.auth.currentUser!!.uid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followList.clear()
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    followList.add(user!!)
                }
                followAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }
}