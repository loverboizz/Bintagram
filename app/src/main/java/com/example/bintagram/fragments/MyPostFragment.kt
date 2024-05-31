package com.example.bintagram.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bintagram.Models.Post
import com.example.bintagram.R
import com.example.bintagram.adapters.MyPostRvAdapter
import com.example.bintagram.adapters.PostAdapter
import com.example.bintagram.databinding.FragmentMyPostBinding
import com.example.bintagram.utils.POST
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MyPostFragment(val userUid:String) : Fragment(), MyPostRvAdapter.OnItemClickListener{
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
        adapter = MyPostRvAdapter(requireContext(), postList, this)

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
                        if (post != null && post.uid == userUid   ) { // Check the uid of the current user
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

    override fun onItemClick(data: String) {
//        val fragment = PostDetailFragment().apply {
//            arguments = Bundle().apply {
//                putString("position", data)
//            }
//        }

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.fragment_post_detail)

        val postDetail: RecyclerView = dialog.findViewById(R.id.post_detail)
        var postAdapter: PostAdapter
        var postList= ArrayList<Post>()
        var specificPostPosition: Int = RecyclerView.NO_POSITION
        specificPostPosition = data.toInt()

        mDbRef = FirebaseDatabase.getInstance().getReference()
        postAdapter=  PostAdapter(requireContext(), postList)
        postDetail.layoutManager= LinearLayoutManager(context)
        postDetail.adapter=postAdapter
        postAdapter.scrollToLastClickedPosition(postDetail)


        postDetail.apply {
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
                Toast.makeText(context, "ERROR: $error", Toast.LENGTH_LONG).show() // Handle errors
            }
        })
        postDetail.scrollToPosition(specificPostPosition)



        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

        // Thay thế Fragment hiện tại bằng Fragment mới
//        childFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, fragment)
//            .addToBackStack(null)
//            .commit()
    }


}
