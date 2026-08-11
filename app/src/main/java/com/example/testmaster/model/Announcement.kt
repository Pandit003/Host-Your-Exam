package com.example.testmaster.model

import java.io.Serializable

data class Announcement(
    val id: String? = null,
    val announcerUid: String? = null,
    val announcerName: String? = null,
    val title: String? = null,
    val description: String? = null,
    val examDate: String? = null,
    val announcementDate: String? = null,
    val duration: String? = null,
    val noOfQuestions: String? = null,
    val markingPattern: String? = null,
    val type: String? = "EXAM"
) : Serializable
