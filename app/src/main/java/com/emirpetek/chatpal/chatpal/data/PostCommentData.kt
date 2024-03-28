package com.emirpetek.chatpal.chatpal.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PostCommentData(val postKey:String, val commentSharerUserKey:String, val commentSharerUsername:String,
    val commentContent:String, val commentDate:String) {

    constructor() : this("","","","","")
}