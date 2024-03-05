package com.emirpetek.chatpal.chatpal

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FollowData(val followerUserKey:String? = null,
                      val followingUserKey:String? = null,
                      val followerFollowDate:String? = null,
                      val followerFollowState:String? = null,
                      val followerUnfollowDate:String? = null) {

    constructor(): this("","","","","")
}