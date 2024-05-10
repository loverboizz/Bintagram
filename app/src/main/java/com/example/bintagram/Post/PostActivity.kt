package com.example.bintagram.Post

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bintagram.HomeActivity
import com.example.bintagram.Models.Post
import com.example.bintagram.databinding.ActivityPostBinding
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.POST_FOLDER
import com.example.bintagram.utils.USER_NODE
import com.example.bintagram.utils.uploadImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }
    private lateinit var mDbRef: DatabaseReference
    var imageUrl:String? = null
    private val launcher= registerForActivityResult(ActivityResultContracts.GetContent()){
            uri->
        uri?.let {
            uploadImage(uri, POST_FOLDER){
                url->
                if (url!=null){
                    binding.selectImage.setImageURI(uri)
                    imageUrl=url
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.materialToolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(Intent(this@PostActivity,HomeActivity::class.java))
            finish()
        }

        binding.selectImage.setOnClickListener{
            launcher.launch("image/*")
        }

        binding.cancelButton.setOnClickListener{
            startActivity(Intent(this@PostActivity,HomeActivity::class.java))
            finish()
        }

        mDbRef= FirebaseDatabase.getInstance().getReference()

        binding.postButton.setOnClickListener{
            Firebase.firestore.collection(USER_NODE).document().get().addOnSuccessListener {
                val post:Post= Post(postId = mDbRef.push().key!!,
                    postUrl =imageUrl!!,
                    caption = binding.caption.editText?.text.toString(),
                    uid = Firebase.auth.currentUser!!.uid,
                    time= System.currentTimeMillis().toString())


                mDbRef.child(POST).child(post.postId).setValue(post).addOnSuccessListener {
                    Firebase.firestore.collection(Firebase.auth.currentUser!!.uid).document().set(post).addOnSuccessListener{
                        startActivity(Intent(this@PostActivity,HomeActivity::class.java))
                        finish()
                    }
                }
//                Firebase.firestore.collection(POST).document().set(post).addOnSuccessListener {
//                    Firebase.firestore.collection(Firebase.auth.currentUser!!.uid).document().set(post).addOnSuccessListener {
//                        startActivity(Intent(this@PostActivity,HomeActivity::class.java))
//                        finish()
//                    }
//                }

            }

        }
    }
}