package com.emirpetek.chatpal.chatpal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FriendlistData(val usernameSecond:String,val date:String,val friendState:String, val key:String,val usernameFirst:String) {

    constructor() : this("","","","","")
}