package com.emirpetek.chatpal.chatpal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UsersWithUserKey(val userKey:String? = null,val user:Users? = null) {
}