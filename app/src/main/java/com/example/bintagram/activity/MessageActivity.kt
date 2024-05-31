package com.example.bintagram.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bintagram.Models.Message
import com.example.bintagram.Models.User
import com.example.bintagram.adapters.MessageAdapter
import com.example.bintagram.databinding.ActivityMessageBinding
import com.example.bintagram.utils.CHAT
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MessageActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    var receiverRoom: String? = null
    var senderRoom: String? = null
    private lateinit var mDbRef: DatabaseReference
    val binding by lazy {
        ActivityMessageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title= ""
        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this@MessageActivity, ChatActivity::class.java))
            finish()
        }

        mDbRef = FirebaseDatabase.getInstance().getReference()
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        senderRoom= receiverUid + senderUid
        receiverRoom =senderUid+ receiverUid


        mDbRef.child(USER_NODE).child(receiverUid!!).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)!!
            binding.name.text = user.name
            if(user.image.isNullOrEmpty()){

            }
            else{
                Picasso.get().load(user.image).into(binding.profileImage)
            }
        }

//        Firebase.firestore.collection(USER_NODE).document(receiverUid!!).get().addOnSuccessListener {
//            val user: User = it.toObject<User>()!!
//            binding.name.text = user.name
//            if(user.image.isNullOrEmpty()){
//
//            }
//            else{
//                Picasso.get().load(user.image).into(binding.profileImage)
//            }
//        }

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messageRecyclerView.adapter =messageAdapter

        // load message to adapter
        mDbRef.child(CHAT).child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()
                    for(postSnapshot in snapshot.children){

                        val message= postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)

                    }
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        binding.sendButton.setOnClickListener {


            val message = binding.messageBox.text.toString()
            if (message!=""){
                val messageObject = Message(message, senderUid)
                mDbRef.child(CHAT).child(senderRoom!!).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        mDbRef.child(CHAT).child(receiverRoom!!).child("messages").push()
                            .setValue(messageObject)
                    }
                binding.messageBox.setText("")
            }

        }


    }
}