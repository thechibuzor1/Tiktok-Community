package com.downbadbuzor.tiktok.model

data class UserModel(
    var id: String = "",
    var email: String = "",
    var username: String = "",
    var profilePic: String = "",
    var followerList: MutableList<String> = mutableListOf(),
    var followingList: MutableList<String> = mutableListOf(),
    var liked: MutableList<String> = mutableListOf(),
    var starred: MutableList<String> = mutableListOf(),
    var bio: String = "**No bio yet**"
)
