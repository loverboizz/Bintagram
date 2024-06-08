package com.example.bintagram.Post

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bintagram.activity.HomeActivity
import com.example.bintagram.Models.Post
import com.example.bintagram.databinding.ActivityPostBinding
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.POST_FOLDER
import com.example.bintagram.utils.uploadImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.yalantis.ucrop.UCrop
import java.io.File

class PostActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }
    private lateinit var mDbRef: DatabaseReference
    var imageUrl: String? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            startCrop(it)
        }
    }

    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                uploadImage(uri, POST_FOLDER) { url ->
                    if (url != null) {
                        imageUrl = url
                        // Cập nhật ImageView với ảnh đã crop và upload thành công
                        binding.selectImage.setImageURI(uri)
                    } else {
                        Toast.makeText(this@PostActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.materialToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.materialToolbar.setNavigationOnClickListener {
            startActivity(Intent(this@PostActivity, HomeActivity::class.java))
            finish()
        }

        binding.selectImage.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.cancelButton.setOnClickListener {
            startActivity(Intent(this@PostActivity, HomeActivity::class.java))
            finish()
        }

        mDbRef = FirebaseDatabase.getInstance().getReference()

        binding.postButton.setOnClickListener {
            val captionText = binding.caption.editText?.text.toString()
            val userId = Firebase.auth.currentUser?.uid
            val currentTime = System.currentTimeMillis().toString()

            if (imageUrl == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userId == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val post = Post(
                postId = mDbRef.push().key!!,
                postUrl = imageUrl!!,
                caption = captionText,
                uid = userId,
                time = currentTime
            )

            mDbRef.child(POST).child(post.postId).setValue(post).addOnSuccessListener {
                startActivity(Intent(this@PostActivity, HomeActivity::class.java))
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage.jpg"))
        val options = UCrop.Options().apply {
            setFreeStyleCropEnabled(true)
        }
        val intent = UCrop.of(uri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f)  // You can set any aspect ratio you need
            .getIntent(this)

        cropLauncher.launch(intent)
    }
}
