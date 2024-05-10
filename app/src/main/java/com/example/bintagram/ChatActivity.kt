package com.example.bintagram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bintagram.Models.User
import com.example.bintagram.adapters.FollowAdapter
import com.example.bintagram.adapters.MessageUserAdapter
import com.example.bintagram.databinding.ActivityChatBinding
import com.example.bintagram.utils.FOLLOW
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }
    private var followList=ArrayList<User>()
    private lateinit var followAdapter: FollowAdapter
    private var userMessageList = ArrayList<User>()
    private lateinit var userAdapter: MessageUserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        setSupportActionBar(binding.materialToolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(Intent(this@ChatActivity,HomeActivity::class.java))
            finish()
        }
        userAdapter = MessageUserAdapter(this, userMessageList)
        binding.userRv.layoutManager = LinearLayoutManager(this)
        binding.userRv.adapter = userAdapter


        followAdapter = FollowAdapter(this, followList)
        binding.folowRv.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.folowRv.adapter = followAdapter

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tempList = arrayListOf<User>()
                userMessageList.clear()
                Log.d("cr_uid:", Firebase.auth.currentUser!!.uid)

                val querySnapshot = Firebase.firestore.collection(Firebase.auth.currentUser!!.uid + FOLLOW).get().await()

                for (document in querySnapshot.documents) {
                    val user = document.toObject<User>() ?: continue
                    val followQuerySnapshot = Firebase.firestore.collection(user.uid!! + FOLLOW).get().await()

                    for (followDocument in followQuerySnapshot.documents) {
                        val userfl = followDocument.toObject<User>() ?: continue
                        if (userfl.uid == Firebase.auth.currentUser!!.uid) {
                            tempList.add(user)
                            break // No need to continue if the user is found
                        }
                    }
                }
                userMessageList.addAll(tempList)
                withContext(Dispatchers.Main) {
                    userAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting follow list", e)
            }
        }





        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ FOLLOW).get().addOnSuccessListener {
            var tempList= arrayListOf<User>()
            followList.clear()
            for (i in it.documents){
                var user:User=i.toObject<User>()!!
                tempList.add(user)
            }
            followList.addAll(tempList)
            followAdapter.notifyDataSetChanged()
        }

    }
}