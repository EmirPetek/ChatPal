package com.emirpetek.chatpal.chatpal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PostDetailsData(
    val sharerUserKey:String? = null, val sharerUserName:String? = null, val postContent:String? = null, val numberOfLikes:Int? = null,
    val numberOfComments:Int? = null, val shareDate:String? = null, val deleteState:String? = null, val deleteDate:String? = null) {
}