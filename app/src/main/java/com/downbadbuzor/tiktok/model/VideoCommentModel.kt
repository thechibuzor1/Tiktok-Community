package com.downbadbuzor.tiktok.model

import com.google.firebase.Timestamp

data class VideoCommentModel(
    var commentId: String = "",
    var content: String = "",
    var uploaderId: String = "",
    var createdTime: Timestamp = Timestamp.now(),
    var likes: MutableList<String> = mutableListOf()
)