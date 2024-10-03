package com.downbadbuzor.tiktok.model

import com.google.firebase.Timestamp

data class VideoModel(
    var videoId: String = "",
    var title: String = "",
    var url: String = "",
    var uploaderId: String = "",
    var createdTime: Timestamp = Timestamp.now(),
    var likes: MutableList<String> = mutableListOf(),
    var comments: MutableList<String> = mutableListOf()
)
