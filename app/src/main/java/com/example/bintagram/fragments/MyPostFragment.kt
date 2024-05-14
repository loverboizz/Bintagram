package com.example.bintagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bintagram.Models.Post
import com.example.bintagram.adapters.MyPostRvAdapter
import com.example.bintagram.databinding.FragmentMyPostBinding
import com.example.bintagram.utils.POST
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MyPostFragment : Fragment() {
    private lateinit var binding: FragmentMyPostBinding
    private lateinit var mDbRef: DatabaseReference
    private lateinit var postList: ArrayList<Post>
    private lateinit var adapter: MyPostRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyPostBinding.inflate(inflater, container, false)

        // Initialize the list and adapter
        postList = ArrayList()
        adapter = MyPostRvAdapter(requireContext(), postList)

        // Initialize DatabaseReference
        mDbRef = FirebaseDatabase.getInstance().getReference()

        // Set up RecyclerView
        binding.rv.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.adapter = adapter

        // Set up ValueEventListener
        mDbRef.child(POST).orderByKey().limitToLast(1000).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear() // Clear the current post list
                if (snapshot.exists()) {
                    for (i in snapshot.children) {
                        val post = i.getValue(Post::class.java)
                        if (post != null && post.uid == Firebase.auth.currentUser!!.uid) { // Check the uid of the current user
                            postList.add(post) // Add post to the list
                        }
                    }
                    adapter.notifyDataSetChanged() // Notify the adapter about the data change
                }
                postList.reverse() // Reverse the list if needed

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR: $error", Toast.LENGTH_LONG).show() // Handle errors
            }
        })

        return binding.root
    }

    companion object {
    }
}
