package com.example.projectmanager.models

import java.io.Serializable

data class User(
    var id:String?="",
    var name:String?="",
    var email:String?="",
    var image:String?="",
    var fcmToken:String?="",
    var selected:Boolean=false
): Serializable
