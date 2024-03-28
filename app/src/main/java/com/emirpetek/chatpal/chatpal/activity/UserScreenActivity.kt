package com.emirpetek.chatpal.chatpal.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.adapter.ProfileFragmentInterestAdapter
import com.emirpetek.chatpal.chatpal.adapter.UserScreenActivityPostAdapter
import com.emirpetek.chatpal.chatpal.data.FollowData
import com.emirpetek.chatpal.chatpal.data.PostDetailsData
import com.emirpetek.chatpal.chatpal.data.Users
import com.emirpetek.chatpal.chatpal.databinding.ActivityUserScreenBinding
import com.emirpetek.chatpal.chatpal.fragment.ProfileFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserScreenActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserScreenBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapterInterests: ProfileFragmentInterestAdapter
    private lateinit var interestsList:ArrayList<String>
    private var postList:ArrayList<Map<String, PostDetailsData>> = ArrayList()
    private lateinit var adapterPost: UserScreenActivityPostAdapter

    companion object var userkeyMain = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        setSupportActionBar(binding.toolbarFriends)

        //userInformations()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarFriends.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("whereComingFromToMainActivity","anyActivity")
            startActivity(intent)
            finish()
           // onBackPressedDispatcher.onBackPressed()
        }

        val username = intent.getStringExtra("username") // karşı tarafın kullanıcı adı
        binding.toolbarFriends.title = username


            sharedPreferences = getSharedPreferences("userInformation", Context.MODE_PRIVATE)
            userkeyMain = sharedPreferences.getString("userKey", "").toString()

        val receivedIntent = intent
        val userkeyAnother = receivedIntent.getStringExtra("key")

        userInformations()
        btnState(userkeyMain,userkeyAnother!!)
        rvInterests(userkeyAnother)
        bindFollowingAndFollower(userkeyAnother)
        rvPosts(userkeyAnother)
        btnSendMessage(userkeyAnother,username!!)



    }

    fun btnSendMessage(userkeyAnother: String, usernameAnother:String){
        binding.buttonSendMessage.setOnClickListener {
            val intent = Intent(this, MessagesScreenActivity::class.java)
            intent.putExtra("userKeyAnotherUser",userkeyAnother)
            intent.putExtra("usernameAnotherUser",usernameAnother)
            startActivity(intent)
        }

    }

    fun bindFollowingAndFollower(userKey: String){
        followerBindingDataGetter(userKey)
        followingBindingDataGetter(userKey)
    }

    private fun followingBindingDataGetter(userkey: String){

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("follow")//.orderByChild(userkey)

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var followingCounter = 0
                for (i in snapshot.children){
                    for (j in i.children) {
                        val u = j.getValue(FollowData::class.java)
                        if (u != null) {
                            if (u.followerUserKey.equals(userkey)) {
                                followingCounter++
                                Log.e("followeruserkey: ", u.followerUserKey.toString())
                            }
                        }
                    }
                }

                if (followingCounter > 0){
                    binding.textViewProfileFollowing.text = "$followingCounter \n Takip"
                }else{
                    binding.textViewProfileFollowing.text = "0 \n Takip"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun followerBindingDataGetter(userkey:String){

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("follow")

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var followerCounter = 0
                for (i in snapshot.children){
                    for (j in i.children){
                        val u = j.getValue(FollowData::class.java)
                        if (u != null){
                            if (u.followingUserKey.equals(userkey) && u.followerFollowState.equals("follow")){
                                followerCounter++
                                Log.e("followinguserkey: ", u.followingUserKey.toString())
                                Log.e("follower and inguserkey: ", u.followerUserKey.toString())
                            }
                        }
                    }
                }

                if (followerCounter > 0){
                    binding.textViewProfileFollower.text = "$followerCounter \n Takipçi"
                }else{
                    binding.textViewProfileFollower.text = "0 \n Takipçi"

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    fun rvInterests(userKey: String){
        interestsList = ArrayList()
        binding.recyclerViewInterests.setHasFixedSize(true)
        binding.recyclerViewInterests.layoutManager = GridLayoutManager(this,3)
        adapterInterests = ProfileFragmentInterestAdapter(this,interestsList)

        val db = FirebaseDatabase.getInstance().getReference("users").child(userKey)
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                interestsList.clear()
                val userInterests = snapshot.child("userInterests").getValue(String::class.java)
                if (!userInterests.isNullOrEmpty()) {

                    // interestsList.add(userInterests)
                    //dbSelectedItemList.add(data.toString())
                    val part = userInterests.split(",")

                    for (x in part){
                        interestsList.add(x)
                    }
                    adapterInterests.notifyDataSetChanged()
                }

                binding.recyclerViewInterests.adapter = adapterInterests

            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    fun btnState(userkeyMain:String,userkeyAnother:String){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("follow").child(userkeyMain)

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){ // eğer ilgili kullanıcının en az bir takip ettiği varsa

                    for (i in snapshot.children){


                        val u =  i.getValue(FollowData::class.java)
                        if (u != null){
                            Log.e("sıralama:", " ilk if")

                            if (u.followerUserKey.equals(userkeyMain) && u.followingUserKey.equals(userkeyAnother)){
                                val followKey = i!!.key
                                if (u.followerFollowState.equals("follow")){
                                    Log.e("usa f/u", " follow")
                                    btnUnfollow(userkeyMain,followKey!!)
                                }
                                if (u.followerFollowState.equals("unfollow")){
                                    Log.e("usa f/u", " unfollow")

                                    btnUnfollowToFollow(userkeyMain,followKey!!)
                                }
                            }
                            if(u.followerUserKey.equals(userkeyMain) && !u.followingUserKey.equals(userkeyAnother)){
                                Log.e("bu durum","gelen key: $userkeyAnother")
                                Log.e("usa f/u", " 3.if")

                                btnFollow(userkeyMain,userkeyAnother)
                            }
                        }
                    }
                }else{
                    btnFollow(userkeyMain,userkeyAnother)
                    Log.e("usa f/u", " en dış else")

                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    fun btnFollow(userkeyMain:String,userkeyAnother:String){

        binding.buttonFollow.text = "Takip et"

        Log.e("userscreen activity: ", "fonk btnFollow")

        binding.buttonFollow.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("follow").child(userkeyMain)


            val followTime = System.currentTimeMillis().toString()
            val follow = FollowData(userkeyMain,userkeyAnother,followTime,"follow","")
            myRef.push().setValue(follow)
        }

    }

    fun btnUnfollow(userkeyMain:String,followKey:String){
        binding.buttonFollow.text = "Takipten çık"
        Log.e("userscreen activity: ", "fonk btnUnfollow")
        binding.buttonFollow.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("follow")
            val updateInfo = HashMap<String,Any>()
            updateInfo["followerFollowState"] = "unfollow"
            updateInfo["followerUnfollowDate"] = System.currentTimeMillis().toString()
            myRef.child(userkeyMain).child(followKey).updateChildren(updateInfo)
        }
    }

    fun btnUnfollowToFollow(userkeyMain: String,followKey:String){
        binding.buttonFollow.text = "Takip et"
        Log.e("userscreen activity: ", "fonk btnUnfollowToFollow")

        binding.buttonFollow.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("follow")
            val updateInfo = HashMap<String,Any>()
            updateInfo["followerFollowState"] = "follow"
            updateInfo["followerFollowDate"] = System.currentTimeMillis().toString()
            myRef.child(userkeyMain).child(followKey).updateChildren(updateInfo)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun bindAge(birthdate:String, gender:String){
        val age = ProfileFragment().calculateAge(birthdate).toString()
        if (gender.equals("Erkek")){
            binding.textViewUserScreenAge.setTextColor(Color.BLUE)
            binding.imageViewUserScreenAgeIcon.setImageResource(R.drawable.baseline_man_24)
            binding.textViewUserScreenAge.text = age
        }else{
            binding.textViewUserScreenAge.setTextColor(resources.getColor(R.color.girl_color))
            binding.imageViewUserScreenAgeIcon.setImageResource(R.drawable.baseline_woman_2_24)
            binding.textViewUserScreenAge.text = age
        }
    }

    fun rvPosts(userKey: String){
        postList = ArrayList()
        binding.recyclerViewUserScreenPost.setHasFixedSize(true)
        binding.recyclerViewUserScreenPost.layoutManager = LinearLayoutManager(this)
        adapterPost = UserScreenActivityPostAdapter(this,postList)
        binding.recyclerViewUserScreenPost.adapter = adapterPost

        val ref = FirebaseDatabase.getInstance().getReference("posts").orderByChild("deleteState").equalTo("active")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()

                for (i in snapshot.children){
                    val post = i.getValue(PostDetailsData::class.java)
                    if (post != null && post.sharerUserKey.equals(userKey)){
                        val postKey = i.key!!
                        val postMap: HashMap<String, PostDetailsData> = HashMap()
                        postMap.put(postKey,post)
                        postList.add(postMap)
                        updateLikeNumber(postKey)
                        updateCommentNumber(postKey)
                        adapterPost.notifyDataSetChanged()
                    }
                }
                postList.sortByDescending { it.values.firstOrNull()?.shareDate }
                //postList = postList.sortedByDescending { it.shareDate } as ArrayList<Map<String, PostDetailsData>>
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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
                        adapterPost.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }
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
                        adapterPost.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }
    }

    fun userInformations(){
        // friendsfragment
        val receivedIntent = intent
        val userKey = receivedIntent.getStringExtra("key")!!
        bindUserInformation(userKey)
    }

    fun bindUserInformation(userKey:String){
        val db = FirebaseDatabase.getInstance().getReference("users").orderByChild(userKey)
        db.addValueEventListener(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val user = i.getValue(Users::class.java)
                    if (user != null && i.key.equals(userKey)){
                        Log.e("user: " ,user.toString())

                        binding.textViewUserScreenFullname.text = user.fullname//"Ad Soyad: " + user.fullname
                        binding.textViewUserScreenCity.text = user.city//"Yaşadığı şehir: " + user.city
                        binding.toolbarFriends.title = user.username

                        if (user.userAboutMe.equals("")){
                            binding.textViewUserScreenAboutMe.text = "Hakkımda \n Hiçbir bilgi girilmemiş."
                        }else {
                            binding.textViewUserScreenAboutMe.text = "Hakkımda \n " + user.userAboutMe
                        }

                        Log.e("user birthdate", user.birthdate!!)
                        bindAge(user.birthdate,user.gender!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_friend,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_block_user -> {
                Toast.makeText(applicationContext,"engelle tıklanıd", Toast.LENGTH_SHORT).show()
                return true
            }
            androidx.appcompat.R.id.home -> {
                finish()
                //startActivity(Intent(this,MainActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


}