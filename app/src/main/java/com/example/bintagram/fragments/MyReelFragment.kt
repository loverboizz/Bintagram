package com.example.bintagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bintagram.Models.Post
import com.example.bintagram.Models.Reel
import com.example.bintagram.adapters.MyReelAdapter
import com.example.bintagram.databinding.FragmentMyReelBinding
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.REEL
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class MyReelFragment(val userUid: String) : Fragment() {
    private lateinit var binding: FragmentMyReelBinding
    private lateinit var mDbRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentMyReelBinding.inflate(inflater, container, false)
        val reelList= ArrayList<Reel>()
        val adapter = MyReelAdapter(requireContext(),reelList)
        binding.rv.layoutManager= StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.adapter=adapter


        mDbRef = FirebaseDatabase.getInstance().getReference()

        mDbRef.child(REEL).orderByKey().limitToLast(1000).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reelList.clear() // Clear the current post list
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        val reel = i.getValue(Reel::class.java)
                        if (reel != null && reel.uid == userUid   ) { // Check the uid of the current user
                            reelList.add(reel) // Add post to the list
                        }
                    }
                    adapter.notifyDataSetChanged() // Notify the adapter about the data change
                }
                reelList.reverse() // Reverse the list if needed

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR: $error", Toast.LENGTH_LONG).show() // Handle errors
            }
        })


//        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+ REEL).get().addOnSuccessListener {
//            val tempList= arrayListOf<Reel>()
//            for (i in it.documents){
//                val reel: Reel = i.toObject<Reel>()!!
//                tempList.add(reel)
//            }
//            reelList.addAll(tempList)
//            adapter.notifyDataSetChanged()
//        }
        return binding.root
    }

    companion object {

    }
}