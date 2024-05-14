package com.example.bintagram.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bintagram.Models.Post
import com.example.bintagram.adapters.PostAdapter
import com.example.bintagram.databinding.ActivityViewPostBinding
import com.example.bintagram.fragments.ProfileFragment
import com.example.bintagram.utils.POST
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ViewPostActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityViewPostBinding.inflate(layoutInflater)
    }
    private lateinit var mDbRef: DatabaseReference
    private var postList= ArrayList<Post>()
    lateinit var postAdapter: PostAdapter
    private var specificPostPosition: Int = RecyclerView.NO_POSITION
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setSupportActionBar(binding.materialToolbar2)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title= "Post"
        binding.materialToolbar2.setNavigationOnClickListener {
            startActivity(Intent(this@ViewPostActivity, ProfileFragment::class.java))
            finish()
        }
        val lastPosition = intent.getStringExtra("lastPosition")
        specificPostPosition = lastPosition?.toInt() ?: RecyclerView.NO_POSITION

        mDbRef = FirebaseDatabase.getInstance().getReference()
        postAdapter=  PostAdapter(this, postList)
        binding.postDetail.layoutManager= LinearLayoutManager(this)
        binding.postDetail.adapter=postAdapter
        postAdapter.scrollToLastClickedPosition(binding.postDetail)


        binding.postDetail.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this.context)

        }
        mDbRef.child(POST).orderByKey().limitToLast(1000).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear() // Clear the current post list
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        val post = i.getValue(Post::class.java)
                        if (post != null && post.uid == Firebase.auth.currentUser!!.uid) { // Check the uid of the current user
                            postList.add(post) // Add post to the list
                        }
                    }
                    postAdapter.notifyDataSetChanged() // Notify the adapter about the data change
                }
                postList.reverse() // Reverse the list if needed

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewPostActivity, "ERROR: $error", Toast.LENGTH_LONG).show() // Handle errors
            }
        })
        binding.postDetail.scrollToPosition(specificPostPosition)






    }
}