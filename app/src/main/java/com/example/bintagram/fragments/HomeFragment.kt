
package com.example.bintagram.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bintagram.Models.Post
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.activity.ChatActivity
import com.example.bintagram.activity.NotificationActivity
import com.example.bintagram.adapters.FollowAdapter
import com.example.bintagram.adapters.PostAdapter
import com.example.bintagram.databinding.FragmentHomeBinding
import com.example.bintagram.utils.FOLLOW
import com.example.bintagram.utils.POST
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mDbRef: DatabaseReference
    private var postList= ArrayList<Post>()
    private lateinit var postAdapter: PostAdapter
    private var followList=ArrayList<User>()
    private lateinit var followAdapter: FollowAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        binding= FragmentHomeBinding.inflate(inflater, container, false)
        postAdapter= PostAdapter(requireContext(), postList)
        binding.postRv.layoutManager=LinearLayoutManager(requireContext())
        binding.postRv.adapter=postAdapter
        binding.postRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this.context)

        }

        followAdapter = FollowAdapter(requireContext(), followList)
        binding.folowRv.layoutManager=LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.folowRv.adapter = followAdapter

        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.materialToolbar2)

        mDbRef = FirebaseDatabase.getInstance().getReference()

//        mDbRef.child(FOLLOW).child(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
//            followList.clear()
//            for (i in it.children){
//                val uid = i.key!!
//                followList.add(uid)
//
//            }
//            followAdapter.notifyDataSetChanged()
//        }


        mDbRef.child(FOLLOW).child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                followList.clear()
                for (i in snapshot.children){
                    var user = i.getValue(User::class.java)
                    followList.add(user!!)
                }
                followAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })



        mDbRef.child(POST).orderByKey().limitToLast(1000).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                if (snapshot.exists()){
                    for (i in snapshot.children){
                        val posts = i.getValue(Post::class.java)
                        postList.add(posts!!)
                    }
                }
                postList.reverse()
                postAdapter= PostAdapter(binding.root.context, postList)
                binding.postRv.adapter=postAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "ERROR: $error", Toast.LENGTH_LONG).show()
            }

        })


        binding.materialToolbar2.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.message -> {
                    val intent = Intent(activity, ChatActivity::class.java) // Specify the Activity you want to navigate to
                    startActivity(intent)

                    true // Trả về true để chỉ ra rằng sự kiện đã được xử lý
                }
                R.id.notification -> {
                    val intent = Intent(activity, NotificationActivity::class.java) // Specify the Activity you want to navigate to
                    startActivity(intent)

                    true // Trả về true để chỉ ra rằng sự kiện đã được xử lý
                }
                else -> false // Trả về false để báo rằng sự kiện chưa được xử lý
            }
        }



        return binding.root
    }

    override fun onStart() {
        super.onStart()
//        Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
//            val user:User = it.toObject<User>()!!
//            if (user.image.isNullOrEmpty()){
//
//            }
//            else{
//                Picasso.get().load(user.image).into(binding.profileImage)
//            }
//        }
        mDbRef.child(USER_NODE).child(Firebase.auth.currentUser!!.uid).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)
            if (user != null) {
                if (!user.image.isNullOrEmpty()) {
                    Picasso.get().load(user.image).into(binding.profileImage)
                }
            }
        }
    }

    companion object {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}
