package com.example.bintagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bintagram.Models.Reel
import com.example.bintagram.adapters.ReelAdapter
import com.example.bintagram.databinding.FragmentReelBinding
import com.example.bintagram.utils.REEL
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ReelFragment : Fragment() {

    private lateinit var binding: FragmentReelBinding
    private lateinit var mDbRef: DatabaseReference
    private lateinit var reelAdapter: ReelAdapter
    private var reelList= ArrayList<Reel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=FragmentReelBinding.inflate(inflater, container, false)
        reelAdapter = ReelAdapter(requireContext(), reelList)
        binding.viewPager.adapter= reelAdapter


//        Firebase.firestore.collection(REEL).get().addOnSuccessListener {
//
//            var tempList= ArrayList<Reel>()
//            reelList.clear()
//            for (i in it.documents){
//                var reel = i.toObject<Reel>()!!
//                tempList.add(reel)
//            }
//            reelList.addAll(tempList)
//            reelList.reverse()
//            reelAdapter.notifyDataSetChanged()
//        }
        mDbRef = FirebaseDatabase.getInstance().getReference()
        mDbRef.child(REEL).orderByKey().limitToLast(1000).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reelList.clear()
                if (snapshot.exists()){
                    for (i in snapshot.children){
                        val reels = i.getValue(Reel::class.java)
                        reelList.add(reels!!)
                    }
                }
                reelList.reverse()
                reelAdapter= ReelAdapter(binding.root.context, reelList)
                binding.viewPager.adapter=reelAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR: $error", Toast.LENGTH_LONG).show()
            }

        })

        return binding.root
    }

    companion object {

    }
}