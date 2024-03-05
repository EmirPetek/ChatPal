package com.emirpetek.chatpal.chatpal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class InterestsTopicData(val title:String, val logo:Int, val mList:List<InterestsElementData>) {
}