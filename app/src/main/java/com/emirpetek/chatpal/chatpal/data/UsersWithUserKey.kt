package com.emirpetek.chatpal.chatpal.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UsersWithUserKey(val userKey:String? = null,val user: Users? = null) {
}