package com.example.testmaster.model

import java.io.Serializable

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "", // "SUBSCRIBE", "ANNOUNCEMENT", "EXAM_HOST"
    val fromUserId: String = "",
    val fromUserName: String = "",
    val fromUserImage: String = "",
    val targetId: String = "", // Exam ID or Announcement ID if applicable
    var isRead: Boolean = false
) : Serializable {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", System.currentTimeMillis(), "", "", "", "", "", false)
}
