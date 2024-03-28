package com.emirpetek.chatpal.chatpal.data

data class FriendRequestData(var usernameSender:String,var sendDate:String, var messageOfRequest:String,var key:String, var requestStatus:String) {

    constructor() : this("","","","","")
}