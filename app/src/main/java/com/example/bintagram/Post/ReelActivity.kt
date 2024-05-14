package com.example.bintagram.Post

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bintagram.Models.Reel
import com.example.bintagram.activity.HomeActivity
import com.example.bintagram.databinding.ActivityReelBinding
import com.example.bintagram.utils.REEL
import com.example.bintagram.utils.REEL_FOLDER
import com.example.bintagram.utils.uploadVideo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class ReelActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityReelBinding.inflate(layoutInflater)
    }
    private lateinit var mDbRef: DatabaseReference
    private lateinit var videoUrl:String
    lateinit var progressDialog:ProgressDialog
    private val launcher= registerForActivityResult(ActivityResultContracts.GetContent()){
            uri->
        uri?.let {
            uploadVideo(uri, REEL_FOLDER, progressDialog){
                    url->
                if (url!=null){

                    videoUrl=url
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        progressDialog= ProgressDialog(this)
        binding.selectReel.setOnClickListener{
            launcher.launch("video/*")
        }

        binding.cancelButton.setOnClickListener{
            startActivity(Intent(this@ReelActivity, HomeActivity::class.java))
            finish()
        }
        mDbRef= FirebaseDatabase.getInstance().getReference()

        binding.postButton.setOnClickListener{
            val reel: Reel = Reel(reelId = mDbRef.push().key!!,
                reelUrl = videoUrl,
                caption=binding.caption.editText?.text.toString(),
                uid = Firebase.auth.currentUser!!.uid)


            mDbRef.child(REEL).child(reel.reelId).setValue(reel).addOnSuccessListener {
                startActivity(Intent(this@ReelActivity, HomeActivity::class.java))
                finish()
            }
        }
    }
}