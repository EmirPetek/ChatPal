package com.emirpetek.chatpal.chatpal.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.emirpetek.chatpal.chatpal.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.adapter.ProfileFragmentInterestAdapter
import com.emirpetek.chatpal.chatpal.adapter.ProfileFragmentPostAdapter
import com.emirpetek.chatpal.chatpal.data.FollowData
import com.emirpetek.chatpal.chatpal.data.FriendlistData
import com.emirpetek.chatpal.chatpal.data.PostDetailsData
import com.emirpetek.chatpal.chatpal.data.Users
import com.google.android.material.bottomnavigation.BottomNavigationView


class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userInformationList:ArrayList<Users>
    private lateinit var adapterInterests: ProfileFragmentInterestAdapter
    private lateinit var adapterPost: ProfileFragmentPostAdapter
    private lateinit var interestsList:ArrayList<String>
    private var postList:ArrayList<Map<String, PostDetailsData>> = ArrayList()
    private var numberOfFriends = 0
    private var mContext: Context? = null
    private var userKey = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        userInformationList = ArrayList()
        userInformationGetter()

        sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val usernameMain = sharedPreferences.getString("usernameMain", "")
        userKey = sharedPreferences.getString("userKey", "").toString()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbarProfile)
        setHasOptionsMenu(true)
        rvInterests(userKey!!)
        rvPosts(userKey)

        binding.toolbarProfile.title = "Kullanıcı Profili"
        binding.toolbarProfile.setTitleTextColor(Color.BLACK)

        followerBindingDataGetter(userKey)
        followingBindingDataGetter(userKey)

        val bottomView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomView.visibility = View.VISIBLE


        binding.progressBarProfileFragment.visibility = View.VISIBLE // progressbar görünür olur

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun profileTextViewSetter(){
        Log.e("liste sizesi",userInformationList.size.toString())
        for (user in userInformationList){
            binding.textViewProfileFullname.text = user.fullname
            val age = calculateAge(user.birthdate!!)
            Log.e("yaş: ", user.birthdate + age.toString())
            val gender = user.gender
            if (gender.equals("Erkek")){
                binding.textViewProfileAge.setTextColor(Color.BLUE)
                binding.imageViewAgeIcon.setImageResource(R.drawable.baseline_man_24)
                binding.textViewProfileAge.text = age.toString()
            }else{
                binding.textViewProfileAge.setTextColor(16711883)
                binding.imageViewAgeIcon.setImageResource(R.drawable.baseline_woman_2_24)
                binding.textViewProfileAge.text = age.toString()
            }

            val aboutMeText = user.userAboutMe.toString()
            if (aboutMeText.equals("null")){
                binding.textViewUserScreenAboutMe.text = "Hakkımda"
                createOrChangeAboutMe("Hakkımda")
            }else{
                binding.textViewUserScreenAboutMe.text = aboutMeText
                createOrChangeAboutMe(aboutMeText)
            }

            manageInterests(fragmentManager)

        }
    }

    fun manageInterests(supportFragmentManager: FragmentManager?) {
        binding.imageButtonProfileAddInterests.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_fragmentProfile_to_fragmentInterests)
        }

    }

    fun rvPosts(userKey: String){
        postList = ArrayList()
        binding.recyclerViewProfilePost.setHasFixedSize(true)
        binding.recyclerViewProfilePost.layoutManager = LinearLayoutManager(mContext)
        adapterPost = ProfileFragmentPostAdapter(requireContext(),postList,this)
        binding.recyclerViewProfilePost.adapter = adapterPost

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

    fun rvInterests(userKey: String){
        interestsList = ArrayList()
        binding.recyclerViewInterests.setHasFixedSize(true)
        binding.recyclerViewInterests.layoutManager = GridLayoutManager(mContext,3)
        adapterInterests = ProfileFragmentInterestAdapter(requireContext(),interestsList)

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



    private fun createOrChangeAboutMe(aboutMeText: String) {
        binding.imageButtonProfileCreateAboutMe.setOnClickListener {
            val bindAlert = layoutInflater.inflate(R.layout.alert_create_about_me, null)
            val editTextProfileAlertAboutMe = bindAlert.findViewById<EditText>(R.id.editTextProfileAlertAboutMe)
            val textViewProfileAlertAboutMe = bindAlert.findViewById<TextView>(R.id.textViewProfileAlertAboutMe)

            val ad = AlertDialog.Builder(requireContext())
            ad.setTitle(R.string.about_me)
            ad.setIcon(R.drawable.baseline_create_24)

            ad.setView(bindAlert)
            editTextProfileAlertAboutMe.setText(aboutMeText)

            ad.setPositiveButton(R.string.add){ dialogInterface: DialogInterface, i: Int ->
                val editTextAboutMe = editTextProfileAlertAboutMe.text.toString()
                sharedPreferences = requireActivity().getSharedPreferences("userInformation",Context.MODE_PRIVATE)
                val userkeyMain = sharedPreferences.getString("userKey", "")!!

                if (editTextAboutMe.length > 100){
                    Toast.makeText(requireContext(),"200 karakterden fazla veri giremezsiniz.",Toast.LENGTH_SHORT).show()
                }else {
                    val ref =
                        FirebaseDatabase.getInstance().getReference("users").child(userkeyMain)
                    val updateAboutMe = HashMap<String, Any>()

                    updateAboutMe["userAboutMe"] = editTextAboutMe
                    ref.updateChildren(updateAboutMe)
                }
            }

            ad.setNegativeButton(R.string.cancel){ dialogInterface:DialogInterface, i:Int ->
                Toast.makeText(requireContext(), R.string.process_cancelled,Toast.LENGTH_SHORT).show()

            }

            ad.show()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }


    private fun followingBindingDataGetter(userkey: String){

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("follow").child(userkey!!)

        myRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var followingCounter = 0
                for (i in snapshot.children){
                        val u = i.getValue(FollowData::class.java)
                        if (u != null){
                            if (u.followerUserKey.equals(userkey)){
                                followingCounter++
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
                            if (u.followerUserKey.equals(userkey)){
                                followerCounter++
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateAge(dateString: String): Int {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val birthDate = LocalDate.parse(dateString, formatter)
        val currentDate = LocalDate.now()
        val age = ChronoUnit.YEARS.between(birthDate, currentDate).toInt()
        return age
    }

    private fun userInformationGetter(){
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")

        sharedPreferences = requireActivity().getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val usernameMain = sharedPreferences.getString("usernameMain", "")


        val query = myRef.orderByChild("username").equalTo(usernameMain)
        query.addValueEventListener(object: ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val u = i.getValue(Users::class.java)
                    if (u != null){
                        val fullname = u.fullname
                        val email = u.email
                        val password = u.password
                        val username = u.username
                        val birthdate = u.birthdate
                        val gender = u.gender
                        val city = u.city
                        val registerDate = u.registerDate
                        val photo = u.photo
                        val lastSeen = u.lastSeen
                        val activityStatus = u.activityStatus
                        val aboutMe = u.userAboutMe
                        val obj = Users(fullname,email,password, username, birthdate, gender, city, registerDate, photo, lastSeen, activityStatus,aboutMe)
                        userInformationList.add(obj)
                        profileTextViewSetter()
                    }
                }
                Log.e("profile fragment", " veriler geliyor size: " + userInformationList.size.toString() )

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        val databaseReference = FirebaseDatabase.getInstance().reference.child("friends")
        databaseReference.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (i in snapshot.children){

                    val u = i.getValue(FriendlistData::class.java)
                    if (u != null){
                        if (u.usernameFirst.equals(usernameMain) || u.usernameSecond.equals(usernameMain)){
                            count++
                        }
                        numberOfFriends = count
                        //binding.textViewNumberOfFriends.text = "Arkadaş Sayısı: " +  numberOfFriends.toString()
                        Log.e("numberoffriends profilefragment: ", numberOfFriends.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_profile,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.action_profile_settings -> {
                val fragment = ProfileSettingsFragment()
                val bundle : Bundle = Bundle()
                bundle.putString("userkey",userKey)
                findNavController().navigate(R.id.action_fragmentProfile_to_profileSettingsFragment,bundle) // profil sayfasından ayarlar sayfasına gidiş
                return true

            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}