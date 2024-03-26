package com.example.bintagram.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.adapters.SearchAdapter
import com.example.bintagram.databinding.FragmentSearchBinding
import com.example.bintagram.utils.USER_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import java.text.Normalizer
import java.util.regex.Pattern

class SearchFragment : Fragment() {
    lateinit var binding: FragmentSearchBinding
    lateinit var adapter: SearchAdapter
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

        Firebase.firestore.collection(USER_NODE).get().addOnSuccessListener {
            var tempList = ArrayList<User>()
            userList.clear()
            for (i in it.documents){
                if (i.id.toString().equals(Firebase.auth.currentUser!!.uid.toString())){

                }
                else{
                    var user:User = i.toObject<User>()!!
                    tempList.add(user)
                }


            }
            userList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }
        binding.searchButton.setOnClickListener{
            var text= binding.searchView.text.toString()

            val searchText = removeDiacritics(text).toLowerCase().trim()

            Firebase.firestore.collection(USER_NODE)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var tempList = ArrayList<User>()
                    userList.clear()

                    querySnapshot.forEach { document ->
                        val name = removeDiacritics(document.getString("name") ?: "").toLowerCase()
                        if (name.contains(searchText)) {
                            val user: User = document.toObject<User>()
                            tempList.add(user)
                        }
                    }

                    userList.addAll(tempList)
                    adapter.notifyDataSetChanged()
                }

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