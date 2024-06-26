package com.example.bintagram.activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bintagram.Models.User
import com.example.bintagram.databinding.ActivitySignUpBinding
import com.example.bintagram.utils.USER_NODE
import com.example.bintagram.utils.USER_PROFILE_FOLDER
import com.example.bintagram.utils.uploadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SignUpActivity : AppCompatActivity() {
    val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    lateinit var user:User
    private lateinit var mDbRef: DatabaseReference
    private val launcher= registerForActivityResult(ActivityResultContracts.GetContent()){
            uri->
        uri?.let {
            uploadImage(uri, USER_PROFILE_FOLDER){
                if (it!=null){
                    user.image=it
                    binding.profileImage.setImageURI(uri)
                }else{
                    user.image=""
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val text ="<font color=#FF000000>Already have an Account</font> <font color=#1E88E5>Login</font>"
        val text2 ="<font color=#FF000000>Already have another Account</font> <font color=#1E88E5>Logout</font>"
        binding.login.setText(Html.fromHtml(text))
        user=User()
        if (intent.hasExtra("MODE")){
            if (intent.getIntExtra("MODE",-1)== 1){
                binding.signUpBtn.text="Update Profile"
                binding.login.setText(Html.fromHtml(text2))
                Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
                    user = it.toObject<User>()!!
                    if (!user.image.isNullOrEmpty()){
                        Picasso.get().load(user.image).into(binding.profileImage)
                    }
                    binding.name.editText?.setText(user.name)
                    binding.email.editText?.setText(user.email)
                    binding.password.editText?.setText(user.uid)

                }

            }
        }
        binding.signUpBtn.setOnClickListener {
            if (intent.hasExtra("MODE")) {
                if (intent.getIntExtra("MODE", -1) == 1) {
                    Firebase.firestore.collection(USER_NODE)
                        .document(Firebase.auth.currentUser!!.uid).set(user)
                        .addOnSuccessListener {
                            startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                            finish()
                        }

                }
            } else {


                if (binding.name.editText?.text.toString().equals("") or
                    binding.email.editText?.text.toString().equals("") or
                    binding.password.editText?.text.toString().equals("")
                ) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Please fill information",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        binding.email.editText?.text.toString(),
                        binding.password.editText?.text.toString()
                    ).addOnCompleteListener {

                            result ->

                        if (result.isSuccessful) {
                            user.name = binding.name.editText?.text.toString()
                            user.caption = ""
                            user.email = binding.email.editText?.text.toString()
                            user.uid = Firebase.auth.currentUser!!.uid

                            Firebase.firestore.collection(USER_NODE)
                                .document(Firebase.auth.currentUser!!.uid).set(user)
                                .addOnSuccessListener {
                                    startActivity(
                                        Intent(
                                            this@SignUpActivity,
                                            HomeActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            mDbRef= FirebaseDatabase.getInstance().getReference()
                            mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).setValue(user)

                        } else {
                            Toast.makeText(
                                this@SignUpActivity,
                                result.exception?.localizedMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            }
        }
        binding.addImage.setOnClickListener{
            launcher.launch("image/*")
        }
        binding.login.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }
    }
}