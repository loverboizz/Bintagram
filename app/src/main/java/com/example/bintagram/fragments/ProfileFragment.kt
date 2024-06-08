package com.example.bintagram.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bintagram.Models.Post
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.activity.LoginActivity
import com.example.bintagram.adapters.PostAdapter
import com.example.bintagram.adapters.SearchAdapter
import com.example.bintagram.adapters.ViewPagerAdapter
import com.example.bintagram.databinding.FragmentProfileBinding
import com.example.bintagram.utils.FOLLOW
import com.example.bintagram.utils.LIKE
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.SAVE
import com.example.bintagram.utils.USER_NODE
import com.example.bintagram.utils.USER_PROFILE_FOLDER
import com.example.bintagram.utils.uploadImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var mDbRef: DatabaseReference
    private lateinit var user: User

    override fun onResume() {
        super.onResume()
        viewPagerAdapter =ViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPagerAdapter.addFragments(MyPostFragment(Firebase.auth.currentUser!!.uid), "My Post")
        viewPagerAdapter.addFragments(MyReelFragment(Firebase.auth.currentUser!!.uid), "My Reel")
        binding.viewPager.adapter=viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    @SuppressLint("RtlHardcoded")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mDbRef = FirebaseDatabase.getInstance().getReference()
        val currentUser = Firebase.auth.currentUser!!

        mDbRef.child(POST).orderByChild("uid").equalTo(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val postCount = snapshot.childrenCount
                    binding.nPost.text = postCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })

        mDbRef.child(FOLLOW).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var followingCount = 0L
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.child(currentUser.uid).exists()) {
                            followingCount++
                        }
                    }
                    binding.nFollower.text = followingCount.toString()

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        mDbRef.child(FOLLOW).child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get the number of followers (number of children under followedUid node)
                    val followerCount = snapshot.childrenCount
                    binding.nFollowing.text = followerCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        binding = FragmentProfileBinding.inflate(inflater, container, false)


        // dialog for update profile
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.edit_profile)
        val name: TextInputLayout= dialog.findViewById(R.id.name)
        val caption: TextInputLayout = dialog.findViewById(R.id.caption)
        val update: Button = dialog.findViewById(R.id.update_button)
        val image : CircleImageView = dialog.findViewById(R.id.profile_update)


        //dialog for change password
        val dialog2 = Dialog(requireContext())
        dialog2.setContentView(R.layout.change_password)
        val currentPw : TextInputLayout = dialog2.findViewById(R.id.current_pw)
        val newPw : TextInputLayout= dialog2.findViewById(R.id.new_pw)
        val confirmPw: TextInputLayout = dialog2.findViewById(R.id.confirm_pw)
        val updatePw: Button = dialog2.findViewById(R.id.update_pw)

        val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uploadImage(uri, USER_PROFILE_FOLDER) { imageUrl ->
                    imageUrl?.let { // Check if imageUrl is not null
                        user.image = imageUrl
                        image.setImageURI(uri)
                    }
                }
            }
        }

        binding.editProfile.setOnClickListener {


            mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)!!
                    name.editText!!.setText(user.name)
                    caption.editText!!.setText(user.caption)
                    if (!user.image.isNullOrEmpty()){
                        Picasso.get().load(user.image).into(image)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


            image.setOnClickListener{
                launcher.launch("image/*")
            }


            update.setOnClickListener {

                user.name = name.editText!!.text.toString()
                user.caption = caption.editText!!.text.toString()

                mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).setValue(user).addOnSuccessListener{
                    binding.name.setText(user.name)
                    binding.caption.setText(user.caption)
                    if (!user.image.isNullOrEmpty()){
                        Picasso.get().load(user.image).into(image)
                    }
                    dialog.dismiss()
                    Toast.makeText(context, "Update profile successfully!!", Toast.LENGTH_LONG).show()
                }


            }

            dialog.show()
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
            dialog.window?.setGravity(Gravity.BOTTOM)
        }

        binding.changePassword.setOnClickListener {


            updatePw.setOnClickListener {

                val CurrentPw = currentPw.editText?.text.toString()
                val NewPw = newPw.editText?.text.toString()
                val ConfirmPw = confirmPw.editText?.text.toString()

                if (CurrentPw.isBlank() || NewPw.isBlank() || ConfirmPw.isBlank()) {
                    Toast.makeText(context, "Please fill all the details", Toast.LENGTH_SHORT)
                        .show()
                } else if (ConfirmPw.equals(NewPw)) {
                    mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).get()
                        .addOnSuccessListener {
                            val user = it.getValue(User::class.java)!!
                            val credential = EmailAuthProvider
                                .getCredential(user.email!!, CurrentPw)
                            currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    currentUser.updatePassword(NewPw)
                                        .addOnCompleteListener { task2 ->
                                            if (task2.isSuccessful) {
                                                dialog2.dismiss()
                                                Toast.makeText(
                                                    context,
                                                    "Update password successfully!!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to update password",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Authentication failed. Incorrect current password!!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            }.addOnFailureListener {
                                Toast.makeText(context, "Something went wrong!!", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }

                }
            }



            dialog2.show()
            dialog2.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog2.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog2.window?.attributes?.windowAnimations = R.style.DialoAnimation
            dialog2.window?.setGravity(Gravity.BOTTOM)
        }


        binding.layoutFlg.setOnClickListener{
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.like_user)
            val likeRecyclerView: RecyclerView = dialog.findViewById(R.id.likeRecyclerView)
            val likeList = ArrayList<User>()
            val likeAdapter: SearchAdapter
            likeAdapter = SearchAdapter(requireContext(), likeList)
            likeRecyclerView.layoutManager= LinearLayoutManager(context)
            likeRecyclerView.adapter =likeAdapter

            var textView: TextView = dialog.findViewById(R.id.textView5)
            textView.setText("Following")

            mDbRef.child(FOLLOW).child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(snapshot: DataSnapshot) {
                    likeList.clear()
                    for(userSnapshot in snapshot.children){

                        val user= userSnapshot.getValue(User::class.java)!!
                            likeList.add(user)
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





        binding.layoutFlr.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.like_user)

            val likeRecyclerView: RecyclerView = dialog.findViewById(R.id.likeRecyclerView)
            val likeList = ArrayList<User>()
            val likeAdapter = SearchAdapter(requireContext(), likeList)
            likeRecyclerView.layoutManager = LinearLayoutManager(context)
            likeRecyclerView.adapter = likeAdapter

            val textView: TextView = dialog.findViewById(R.id.textView5)
            textView.text = "Follower"

            fun fetchUserDetails(userIds: List<String>) {
                val usersRef = mDbRef.child(USER_NODE)
                for (userId in userIds) {
                    usersRef.child(userId).get().addOnSuccessListener {
                        val user = it.getValue(User::class.java)
                        if (user != null) {
                            likeList.add(user)
                            likeAdapter.notifyDataSetChanged()  // Notify adapter when user is added
                        }
                    }.addOnFailureListener {
                        // Handle failure if needed
                    }
                }
            }

            val followingUserIds = mutableListOf<String>()
            mDbRef.child(FOLLOW).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.child(currentUser.uid).exists()) {
                            followingUserIds.add(userSnapshot.key!!)
                        }
                    }
                    fetchUserDetails(followingUserIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })

            dialog.show()
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
        }



        binding.profileImage.setOnClickListener {
//            val intent = Intent(activity, SignUpActivity::class.java)
//            intent.putExtra("MODE",1)
//            activity?.startActivity(intent)
//            activity?.finish()


        }

        binding.more.setOnClickListener {
            val dialogSetting = Dialog(requireContext())
            dialogSetting.setContentView(R.layout.setting_layout)

            val logout: LinearLayout = dialogSetting.findViewById(R.id.sign_out)
            val save: LinearLayout = dialogSetting.findViewById(R.id.save)

            logout.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sign out ??")
                    .setMessage("Are you sure you want to sign out this account ?")
                    .setPositiveButton("Yes") { dialog, which ->
                        Firebase.auth.signOut()
                        startActivity(Intent(activity, LoginActivity::class.java))
                        activity?.finish()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }

            save.setOnClickListener {
                val dialogSave = Dialog(requireContext())
                dialogSave.setContentView(R.layout.fragment_post_detail)

                val postDetail: RecyclerView = dialogSave.findViewById(R.id.post_detail)
                var postAdapter: PostAdapter
                var postList= ArrayList<Post>()

                mDbRef = FirebaseDatabase.getInstance().getReference()
                postAdapter=  PostAdapter(requireContext(), postList)
                postDetail.layoutManager= LinearLayoutManager(context)
                postDetail.adapter=postAdapter
                postAdapter.scrollToLastClickedPosition(postDetail)


                postDetail.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this.context)

                }

                mDbRef.child(SAVE).child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        postList.clear()
                        for (postSnapshot in snapshot.children) {
                            val post = postSnapshot.getValue(Post::class.java)
                            if (post != null) {
                                postList.add(post)
                            }
                        }
                        postAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle possible errors here.
                    }
                })



                dialogSave.show()
                dialogSave.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialogSave.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogSave.window?.attributes?.windowAnimations = R.style.DialoAnimation
                dialogSave.window?.setGravity(Gravity.BOTTOM)
            }




            dialogSetting.show()
            dialogSetting.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            dialogSetting.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogSetting.window?.attributes?.windowAnimations = R.style.DialogAnimation2
            dialogSetting.window?.setGravity(Gravity.RIGHT)
        }



        viewPagerAdapter =ViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPagerAdapter.addFragments(MyPostFragment(Firebase.auth.currentUser!!.uid), "My Post")
        viewPagerAdapter.addFragments(MyReelFragment(Firebase.auth.currentUser!!.uid), "My Reel")
        binding.viewPager.adapter=viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        return binding.root
    }

    companion object {

    }

    override fun onStart() {
        super.onStart()
        mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            if (user != null) {
                binding.name.setTypeface(binding.name.typeface, Typeface.BOLD)
                binding.name.text = user.name ?: ""
                binding.caption.text = user.caption ?: ""

                if (!user.image.isNullOrEmpty()) {
                    Picasso.get().load(user.image).into(binding.profileImage)
                }
            }
        }
    }



}