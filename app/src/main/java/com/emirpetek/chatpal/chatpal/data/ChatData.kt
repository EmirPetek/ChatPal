package com.emirpetek.chatpal.chatpal.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ChatData(val chatID:String? = null, val user1Key:String? = null, val user2Key:String? = null, val user1Username:String? = null,
                    val user2Username:String? = null, val chatCreatedDate:String? = null, val lastMessage:String? = null
                    , val lastMessageTime:String? = null, var numberOfNotReadMessage:String? = null, val lastMessageSender:String? = null ) {
}