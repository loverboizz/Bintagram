package com.example.bintagram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bintagram.Models.User
import com.example.bintagram.adapters.SearchAdapter
import com.example.bintagram.databinding.FragmentSearchBinding
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.Normalizer
import java.util.regex.Pattern

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding
    lateinit var adapter: SearchAdapter
    lateinit var mDbRef: DatabaseReference
    var userList= ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentSearchBinding.inflate(inflater, container, false)
        binding.rv.layoutManager=LinearLayoutManager(requireContext())
        adapter= SearchAdapter(requireContext(),userList)
        binding.rv.adapter=adapter

        mDbRef = FirebaseDatabase.getInstance().getReference()

        mDbRef.child(USER_NODE).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (i in snapshot.children){
                    val users = i.getValue(User::class.java)!!
                    if (Firebase.auth.currentUser?.uid != users.uid){
                        userList.add(users)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

//        Firebase.firestore.collection(USER_NODE).get().addOnSuccessListener {
//            var tempList = ArrayList<User>()
//            userList.clear()
//            for (i in it.documents){
//                if (i.id.toString().equals(Firebase.auth.currentUser!!.uid.toString())){
//
//                }
//                else{
//                    var user:User = i.toObject<User>()!!
//                    tempList.add(user)
//                }
//
//
//            }
//            userList.addAll(tempList)
//            adapter.notifyDataSetChanged()
//        }




        binding.searchButton.setOnClickListener{
            var text= binding.searchView.text.toString()
            val searchText = removeDiacritics(text).toLowerCase().trim()
            mDbRef.child(USER_NODE).addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    for (i in snapshot.children){
                        val users = i.getValue(User::class.java)!!
                        val name = removeDiacritics(users.name!!).toLowerCase().trim()
                        if (name.contains(searchText)){
                            userList.add(users)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        return binding.root
    }

    private fun removeDiacritics(input: String): String {
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }

    companion object {
    }
}