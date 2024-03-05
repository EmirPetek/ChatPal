package com.emirpetek.chatpal.chatpal

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener

@IgnoreExtraProperties
data class Users(val fullname:String? = null, val email:String? = null, val password:String? = null, val username:String? = null
        , val birthdate:String? = null, val gender:String? = null, val city:String? = null, val registerDate:String? = null, val photo:String? = null
                 ,val lastSeen:String? = null,val activityStatus:String? = null, val userAboutMe:String? = null, val userInterests:String? = null) {

        constructor() : this("", "", "", "", "", "", "", "", "","","","","")

}