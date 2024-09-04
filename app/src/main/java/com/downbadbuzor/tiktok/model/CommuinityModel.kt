package com.downbadbuzor.tiktok.model

import com.google.firebase.Timestamp

data class CommuinityModel(
    var  postId : String = "",
    var picture : String? = "",
    var  content : String = "",
    var  uploaderId : String = "",
    var  createdTime : Timestamp = Timestamp.now()

)