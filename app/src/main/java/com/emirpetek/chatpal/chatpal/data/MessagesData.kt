package com.emirpetek.chatpal.chatpal.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MessagesData(val chatKey:String? = null
                        , val senderUserKey:String? = null, val receiverUserKey:String? = null
                        , val senderUsername:String?= null, val receiverUsername:String? = null,
                        val messagesContent:String? = null, val sendTime:String? = null, val messageStatus:String? = null) {
}