package com.emirpetek.chatpal.chatpal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FriendshipData(val usernameFirst:String, val usernameSecond:String, val date:String, val friendState:String) {
}