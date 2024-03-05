package com.emirpetek.chatpal.chatpal

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var postList:ArrayList<Map<String,PostDetailsData>> = ArrayList()
    private lateinit var adapter: HomeFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        val view = binding.root

        binding.toolbarHome.setTitle(R.string.home)
        binding.toolbarHome.setTitleTextColor(Color.BLACK)

        fabSharePost()
        getPostData()

        binding.recyclerViewPosts.setHasFixedSize(true)
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())
        adapter = HomeFragmentAdapter(requireContext(),postList,this)
        binding.recyclerViewPosts.adapter = adapter


        val bottomView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomView.visibility = View.VISIBLE

        return view
    }

    fun getPostData(){
        val ref = FirebaseDatabase.getInstance().getReference("posts").orderByChild("deleteState").equalTo("active")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()

                for (i in snapshot.children){
                    val post = i.getValue(PostDetailsData::class.java)
                    if (post != null){
                        val postKey = i.key!!
                        val postMap: HashMap<String, PostDetailsData> = HashMap()
                        postMap.put(postKey,post)
                        postList.add(postMap)
                        updateLikeNumber(postKey)
                        updateCommentNumber(postKey)
                        adapter.notifyDataSetChanged()
                    }
                }
                postList.sortByDescending { it.values.firstOrNull()?.shareDate }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun updateCommentNumber(postKey: String){
        if (postList.size > 0){
            for (i in 0..<postList.size){
                val numberOfLikes = postList[i].values.firstOrNull()?.numberOfComments
                val db = FirebaseDatabase.getInstance() // veritabanına erişim
                val query = db.getReference("postComments").child(postKey) // yorum sayısını getien query
                query.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val numOfComments = snapshot.childrenCount
                        val updateLikeNumber = HashMap<String,Any>()
                        updateLikeNumber["numberOfComments"] = numOfComments
                        db.getReference("posts").child(postKey).updateChildren(updateLikeNumber) // beğeni sayısını update eder
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }
    }

    fun updateLikeNumber(postKey: String){
        if (postList.size > 0){
            for (i in 0..<postList.size){
                val numberOfLikes = postList[i].values.firstOrNull()?.numberOfLikes
                val db = FirebaseDatabase.getInstance() // veritabanına erişim
                val query = db.getReference("postLikes").child(postKey).orderByChild("likeState").equalTo("like") // beğeni sayısını getien query
                query.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val numOfLikes = snapshot.childrenCount
                        val updateLikeNumber = HashMap<String,Any>()
                        updateLikeNumber["numberOfLikes"] = numOfLikes
                        db.getReference("posts").child(postKey).updateChildren(updateLikeNumber) // beğeni sayısını update eder
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }
    }



    fun fabSharePost(){
        binding.floatingActionButtonSharePost.setOnClickListener {
            val bindAlert = layoutInflater.inflate(R.layout.alert_share_post, null)
            val editTextSharePostContent = bindAlert.findViewById<EditText>(R.id.editTextSharePostContent)

            val ad = AlertDialog.Builder(requireContext())
            ad.setTitle(R.string.share_post)
            ad.setIcon(R.drawable.baseline_create_24)

            ad.setView(bindAlert)

            ad.setPositiveButton(R.string.share) { dialogInterface: DialogInterface, i: Int ->
                val editTextContent = editTextSharePostContent.text?.toString()
                if (editTextContent != null) {
                    if (editTextContent.isEmpty()) {
                        Toast.makeText(context, R.string.enter_anything, Toast.LENGTH_SHORT).show()
                        fabSharePost()
                    } else {
                        val editTextContent = editTextSharePostContent.text?.toString()
                        val shareDate = System.currentTimeMillis().toString()

                        sharedPreferences = requireActivity().getSharedPreferences("userInformation",Context.MODE_PRIVATE)
                        val userkeyMain = sharedPreferences.getString("userKey", "")!!
                        val usernameMain = sharedPreferences.getString("usernameMain", "")!!


                        val post = PostDetailsData(
                            userkeyMain,
                            usernameMain,
                            editTextContent,
                            0,
                            0,
                            shareDate,
                            "active",
                            ""
                        )
                        val ref = FirebaseDatabase.getInstance().getReference("posts")
                        ref.push().setValue(post)

                    }
                }
            }

            ad.setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, i: Int ->
                Toast.makeText(context, R.string.process_cancelled, Toast.LENGTH_SHORT).show()
            }

            ad.show()
        }
    }
}