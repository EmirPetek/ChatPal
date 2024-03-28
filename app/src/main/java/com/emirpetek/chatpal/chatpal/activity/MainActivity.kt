package com.emirpetek.chatpal.chatpal.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.emirpetek.chatpal.chatpal.R
import com.emirpetek.chatpal.chatpal.data.Users
import com.emirpetek.chatpal.chatpal.databinding.ActivityMainBinding
import com.emirpetek.chatpal.chatpal.fragment.PostCommentFragment
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val fragmentManager = supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()
    private lateinit var sharedPreferences: SharedPreferences
    var numBadgeUnreadMessage = 0
    var userkey = ""
    var context:Context = this
    var runOnce = 1
    companion object{
        var whereComingFrom = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = Users()

        val navHostFragment = supportFragmentManager.findFragmentById(androidx.navigation.fragment.R.id.nav_host_fragment_container) as NavHostFragment

        NavigationUI.setupWithNavController(binding.bottomNav,navHostFragment.navController)

        val bundleUserScreen = intent.getBundleExtra("bundleUserScreen")
        val whereComeFrom = intent.getStringExtra("whereComeFrom")

        if (whereComeFrom != null && whereComeFrom == "userScreen" && bundleUserScreen != null) {
            val fragmentPostComment = PostCommentFragment()

            fragmentPostComment.arguments = bundleUserScreen // fragment'a argument olarak bundle'ı ekle
            supportFragmentManager.beginTransaction().replace(R.id.activity_main, fragmentPostComment, "fragmentPostCommentTag").commit()
            // mainactivityden fragmentposta geçiş
        }

        sharedPreferences = getSharedPreferences("userInformation", Context.MODE_PRIVATE)
        val userKey = sharedPreferences.getString("userKey", "")
        val username = sharedPreferences.getString("usernameMain", "")

        Log.e("mainact username: ", username!!)

        userkey = userKey!!

        Log.e("whereComingFrom", whereComingFrom)

    }

    fun setBadgeNumberUnreadMessage(num: Int) {
        binding.bottomNav.getOrCreateBadge(R.id.fragmentMessages).number = num
    }

/*
    override fun onPause() {
        super.onPause()
            Log.e("çalışan lifecycle", "onPause")
            updateUserLastSeen("offline")
    }

    override fun onStop() {
        super.onStop()
     //   if (whereComingFrom != "loginButton") {
            Log.e("çalışan lifecycle", "onStop")
            updateUserLastSeen("offline")
      //  }
    }

    override fun onResume() {
        super.onResume()
      //  if (whereComingFrom != "loginButton") {

            Log.e("çalışan lifecycle", "onResume")
            updateUserLastSeen("online")
       // }
    }

    override fun onRestart() {

        super.onRestart()
    //    if (whereComingFrom != "loginButton") {

            Log.e("çalışan lifecycle", "onRestart")

            updateUserLastSeen("online")
      //  }
    }

    override fun onStart() {


        super.onStart()
     //   if (whereComingFrom != "loginButton") {

            Log.e("çalışan lifecycle", "onStart")

            updateUserLastSeen("online")
     //   }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (whereComingFrom != "loginButton") {

            Log.e("çalışan lifecycle", "onDestroy")

            updateUserLastSeen("offline")
        }
    }*/



    fun updateUserLastSeen(activityStatus:String) {

        val ukey = getSharedPreferences("userInformation", Context.MODE_PRIVATE).getString("userKey","")!!

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")

        val nowDate = System.currentTimeMillis().toString()

        val updateInfo = HashMap<String, Any>()
        updateInfo["activityStatus"] = activityStatus
        updateInfo["lastSeen"] = nowDate

        if (whereComingFrom != "loginButton") {

            myRef.child(userkey).updateChildren(updateInfo)
        }else{

        }
    }

}