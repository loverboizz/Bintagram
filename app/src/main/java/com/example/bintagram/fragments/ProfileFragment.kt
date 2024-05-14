package com.example.bintagram.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.activity.LoginActivity
import com.example.bintagram.adapters.ViewPagerAdapter
import com.example.bintagram.databinding.FragmentProfileBinding
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
        viewPagerAdapter.addFragments(MyPostFragment(), "My Post")
        viewPagerAdapter.addFragments(MyReelFragment(), "My Reel")
        binding.viewPager.adapter=viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mDbRef = FirebaseDatabase.getInstance().getReference()
        val currentUser = Firebase.auth.currentUser!!

        binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.deleteProfile.setOnClickListener {

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
                                                user.uid = NewPw
                                                mDbRef.child(USER_NODE)
                                                    .child(Firebase.auth.currentUser!!.uid)
                                                    .setValue(user)
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

        binding.profileImage.setOnClickListener {
//            val intent = Intent(activity, SignUpActivity::class.java)
//            intent.putExtra("MODE",1)
//            activity?.startActivity(intent)
//            activity?.finish()


        }

        viewPagerAdapter =ViewPagerAdapter(requireActivity().supportFragmentManager)
        viewPagerAdapter.addFragments(MyPostFragment(), "My Post")
        viewPagerAdapter.addFragments(MyReelFragment(), "My Reel")
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
                binding.name.text = user.name ?: ""
                binding.caption.text = user.caption ?: ""

                if (!user.image.isNullOrEmpty()) {
                    Picasso.get().load(user.image).into(binding.profileImage)
                }
            }
        }
    }

}