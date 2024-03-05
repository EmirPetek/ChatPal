package com.emirpetek.chatpal.chatpal

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.databinding.FragmentPostCommentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PostCommentFragment : Fragment() {

    private lateinit var binding:FragmentPostCommentBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var commentList:ArrayList<PostCommentData>
    private lateinit var commentAdapter:PostCommentFragmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostCommentBinding.inflate(inflater,container,false)
        val view = binding.root

        sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val usernameMain = sharedPreferences.getString("usernameMain", "").toString()
        val userKey = sharedPreferences.getString("userKey", "").toString()

       setToolbar()

        val bottomView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomView.visibility = View.GONE

        val bundle = arguments
        val postKey = bundle?.getString("postKey").toString()
        val postUsername = bundle?.getString("postUsername").toString()
        val postShareDate = bundle?.getString("shareDate").toString()
        val postContent = bundle?.getString("postContent").toString()

        postDataCompleter(postUsername,postShareDate,postContent)
        sendComment(postKey,usernameMain,userKey)
        getPostComment(postKey)



     return view
    }


    fun setToolbar(){
        binding.toolbarPostComment.setTitle(R.string.comments)
        binding.toolbarPostComment.setTitleTextColor(Color.BLACK)
        (context as MainActivity).setSupportActionBar(binding.toolbarPostComment) // toolbari actionbar olarak algılar
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)// geri tuşu

        val bundle = arguments
        //userscreenactivitypostadapterdan geliyor

        val whereComeFrom = bundle?.getString("whereComeFrom").toString()
        val postUsername = bundle?.getString("postUsername").toString()
        val postUserKey = bundle?.getString("postUserKey").toString()

        binding.toolbarPostComment.setNavigationOnClickListener {
            Log.e("bundles data: ", "$whereComeFrom, $postUserKey, $postUsername")
            if (whereComeFrom.equals("userScreen")){
                val intent = Intent(requireContext(),UserScreenActivity::class.java)
                intent.putExtra("username",postUsername)
                intent.putExtra("key",postUserKey)
                intent.putExtra("bundlePostComment",bundle)
                Log.e("username key ve bundle:" , "$postUsername , $postUserKey , $bundle")
                startActivity(intent)
            }else {
                findNavController().popBackStack()
                val bottomView =
                    requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
                bottomView.visibility = View.VISIBLE
            }


        }
    }

    fun postDataCompleter(postUsername:String,postShareDate:String,postContent:String){
        binding.textViewPostContent.text = postContent
        binding.textViewPostDate.text = postShareDate
        binding.textViewPostUsername.text = postUsername
    }

    private fun getPostComment(postKey:String){
        commentList = ArrayList()
        binding.recyclerViewPostComment.setHasFixedSize(true)
        binding.recyclerViewPostComment.layoutManager = LinearLayoutManager(requireContext())
        commentAdapter = PostCommentFragmentAdapter(requireContext(),commentList)
        binding.recyclerViewPostComment.adapter = commentAdapter

        val db = FirebaseDatabase.getInstance().getReference("postComments").child(postKey)//.orderByChild("deleteState").equalTo("active")
        db.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (i in snapshot.children){
                    val comment = i.getValue(PostCommentData::class.java)
                    if (comment != null){
                        commentList.add(comment)
                        commentAdapter.notifyDataSetChanged()
                    }

                }
                commentList.sortByDescending { it.commentDate }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        }

    private fun sendComment(postKey:String,username:String,userkey:String){

        binding.buttonPostSendComment.setOnClickListener {
            val editTextComment = binding.editTextPostComment.text.toString()
            val shareDate = System.currentTimeMillis().toString()

            val db = FirebaseDatabase.getInstance().getReference("postComments").child(postKey)
            val comment = PostCommentData(postKey,userkey,username,editTextComment,shareDate)
            db.push().setValue(comment)
            commentAdapter.notifyDataSetChanged()
            binding.editTextPostComment.text.clear()
        }


    }

}