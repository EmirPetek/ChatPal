package com.emirpetek.chatpal.chatpal

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.databinding.FragmentFriendsBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendsFragment : Fragment() {

    private lateinit var binding: FragmentFriendsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: FriendlistAdapter
    private lateinit var friendList:ArrayList<FollowData>

    companion object{
        var isFoundUser: Boolean = true
        var userKey: String? = ""
        var keyForAnotherUser = ""
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendsBinding.inflate(inflater,container,false);
        val view = binding.root;

        fabAddFriend()

        friendList = ArrayList()
        getFriendlistData()
        binding.recyclerViewFriendList.setHasFixedSize(true)
        binding.recyclerViewFriendList.layoutManager = LinearLayoutManager(requireContext())
        adapter = FriendlistAdapter(requireContext(),friendList)
        binding.recyclerViewFriendList.adapter = adapter

        return view;
        }

    private fun getAnotherPersonUserkey(anotherUsername:String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users").orderByChild("username").equalTo(anotherUsername)

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var key = ""
                for (i in snapshot.children){
                    val u = i.getValue(Users::class.java)
                    if (u != null){
                        key = i.key!!
                        Log.e("friendsfragment keyforanotheruser: ", keyForAnotherUser)
                    }
                }
                keyForAnotherUser = key
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    fun getFriendlistData() {
        sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val userkeyMain = sharedPreferences.getString("userKey", "")!!
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("follow")
        Log.e("ukey: ", userkeyMain)

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               friendList.clear()
                    for (i in snapshot.children){

                        for (j in i.children){

                            val u = j.getValue(FollowData::class.java)
                            if (u != null){
                                if (u.followingUserKey.equals(userkeyMain) && u.followerFollowState.equals("follow")){
                                    val followerUserKey = u.followerUserKey // beni takip eden kişi key
                                    val followingUserKey = u.followingUserKey // benim userkey
                                    val fd = FollowData(followerUserKey,followingUserKey,"","","")

                                    usernameFinder(followerUserKey!!) { usernameForAnotherUser ->
                                         val followerUsername = usernameForAnotherUser
                                    }
                                    friendList.add(fd)
                                }
                            }
                        }
                    }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun usernameFinder(userKey: String, callback: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users").orderByKey().equalTo(userKey)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children) {
                    val u = i.getValue(Users::class.java)
                    if (u != null) {
                        val usernameForAnotherUser = u.username!!
                        callback(usernameForAnotherUser)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Error", "Database error: ${error.message}")
            }
        })
    }



    private fun fabAddFriend(){
        binding.floatingActionButtonAddFriend.setOnClickListener {
            Log.e("FAB", "FAB'a tıklandı")
            val bindAlert = layoutInflater.inflate(R.layout.alert_add_friend,null)
            val textInputLayoutAddFriendUsername = bindAlert.findViewById<TextInputLayout>(R.id.editTextAddFriendUsername)
            val editTextAddFriendUsername = textInputLayoutAddFriendUsername.editText

            val ad = AlertDialog.Builder(requireContext())
            ad.setTitle(R.string.find_user)
            ad.setMessage(R.string.enter_looking_for_username)
            ad.setIcon(R.drawable.baseline_people_24)

            ad.setView(bindAlert)
            ad.setPositiveButton(R.string.find_user) { dialog: DialogInterface, i: Int ->
                val username = editTextAddFriendUsername?.text?.toString()
                if (username != null) {
                    if (username.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.enter_anything, Toast.LENGTH_SHORT).show()
                        fabAddFriend()
                    } else {
                        val username = editTextAddFriendUsername?.text?.toString()

                        sharedPreferences = requireContext().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
                        val usernameMain = sharedPreferences.getString("usernameMain", "")

                        if (username.equals(usernameMain)){
                            Toast.makeText(requireContext(), R.string.cannot_send_request_yourself, Toast.LENGTH_SHORT).show()
                        }else{
                           // Log.e("edit ", "text metni " + username)
                        //    Log.e("friendsfragment ", "login uname " + usernameMain.toString()  )
                            val userObj = FindFriend()
                            userObj.userCheck(username, object : UserCheckCallback {
                                override fun onUserCheckResult(isUserFound: Boolean) {
                                    if (!isUserFound) {
                                        Toast.makeText(requireContext(), R.string.no_found_user, Toast.LENGTH_SHORT).show()
                                      //  Log.e("ff isfounduser if fragment ", FindFriend.isFoundUser.toString())
                                    } else {
                                        var intent = Intent(activity,UserScreenActivity::class.java)
                                        var user = FindFriend.userCopy
                                        intent.putExtra("key", userKey)
                                        intent.putExtra("fullname",user.fullname)
                                        intent.putExtra("birthdate",user.birthdate)
                                        intent.putExtra("city",user.city)
                                        intent.putExtra("username",user.username)
                                        intent.putExtra("gender",user.gender)
                                        intent.putExtra("registerDate",user.registerDate)
                                        startActivity(intent)
                                        Log.e("ff isfounduser else fragment ", FindFriend.isFoundUser.toString())
                                    }
                                }
                            })
                        }

                    }
                }
            }
            ad.setNegativeButton(R.string.cancel){
                    dialog: DialogInterface, i -> Int
            }
            ad.create().show()
        }
    }
}