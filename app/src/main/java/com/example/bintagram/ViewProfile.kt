import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.bintagram.Models.User
import com.example.bintagram.R
import com.example.bintagram.activity.MessageActivity
import com.example.bintagram.adapters.ViewPagerAdapter
import com.example.bintagram.fragments.MyPostFragment
import com.example.bintagram.fragments.MyReelFragment
import com.example.bintagram.utils.FOLLOW
import com.example.bintagram.utils.USER_NODE
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class ViewProfile : DialogFragment() {

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    companion object {
        private const val ARG_USER_ID = "userId"

        fun newInstance(userId: String): ViewProfile {
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            val fragment = ViewProfile()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.view_profile)

        val name: TextView = dialog.findViewById(R.id.name)
        val caption: TextView = dialog.findViewById(R.id.caption)
        val profileImage: CircleImageView = dialog.findViewById(R.id.profile_image)
        val follow: Button = dialog.findViewById(R.id.follow)
        val message: Button = dialog.findViewById(R.id.message)

        val viewPager: ViewPager = dialog.findViewById(R.id.viewPager)
        val tabLayout: TabLayout = dialog.findViewById(R.id.tab_layout)
        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        var user: User? = null

        val userId = arguments?.getString(ARG_USER_ID)

        // Fetch user information from Firebase
        var isFollow = false
        mDbRef.child(USER_NODE).child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)
                    user?.let {
                        Glide.with(requireContext()).load(user!!.image).placeholder(R.drawable.avatarr).into(profileImage)
                        name.text = user!!.name
                        caption.text = user!!.caption
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        mDbRef.child(FOLLOW).child(Firebase.auth.currentUser!!.uid).child(userId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                @SuppressLint("ResourceAsColor")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isFollowed = snapshot.exists()
                    if (isFollowed){
                        follow.text = "Unfollow"
                        follow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
                        isFollow = true

                    }else{
                        follow.text="Follow"
                        follow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                        isFollow = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        follow.setOnClickListener {
            mDbRef.child(FOLLOW).child(Firebase.auth.currentUser!!.uid).child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    @SuppressLint("ResourceAsColor")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isFollowed = snapshot.exists()
                        if (isFollowed){
                            snapshot.ref.removeValue()
                            follow.text = "Follow"
                            follow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.blue))
                            isFollow= false
                        }else{
                            snapshot.ref.setValue(user)
                            follow.text = "Unfollow"
                            follow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
                            isFollow=true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

        message.setOnClickListener {
            if(isFollow== false){
                AlertDialog.Builder(context)
                    .setTitle("Message this user")
                    .setMessage("You need to follow this user first!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }else{
                val intent = Intent(requireContext(),MessageActivity::class.java)
                intent.putExtra("uid",userId)
                startActivity(intent)
            }


        }




        // Setup ViewPager with fragments
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragments(MyPostFragment(userId), "Post")
        viewPagerAdapter.addFragments(MyReelFragment(userId), "Reel")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialoAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)

        return dialog
    }
}

