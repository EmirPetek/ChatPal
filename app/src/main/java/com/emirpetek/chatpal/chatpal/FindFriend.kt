package com.emirpetek.chatpal.chatpal

import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface UserCheckCallback {
    fun onUserCheckResult(isUserFound: Boolean)
}

class FindFriend {

    companion object {
        var isFoundUser = false
        var userCopy = Users()
    }

    fun userCheck(username: String?, callback: UserCheckCallback) { // kullanıcıyı arama işlemi
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users")
        val query = myRef.orderByChild("username").equalTo(username)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isUserFound = false
                for (i in snapshot.children) {
                    val user = i.getValue(Users::class.java)
                    if (user != null) {
                        isUserFound = true // Kullanıcı bulundu
                        isFoundUser = true
                        val key = i.key

                        Log.e("*************", "*************")
                        Log.e("key ", key.toString())
                        Log.e("kisi ad", user.fullname.toString())
                        Log.e("kisi email", user.email.toString())
                        userCopy = user.copy(user.fullname,user.email,"",user.username,user.birthdate,user.gender,user.city,user.registerDate,user.photo)
                        FriendsFragment.userKey = key
                        Log.e("ff isfounduser if ", isFoundUser.toString())

                    }
                }

                callback.onUserCheckResult(isUserFound)
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda gerekli işlemleri yap.
                Log.e("Firebase Error", "Error during user check: ${error.message}")
            }
        })
    }
}
