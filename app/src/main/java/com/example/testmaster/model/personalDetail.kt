package com.example.testmaster.model

import java.io.Serializable

class personalDetail(
    val name : String? = null,
    val name_lowercase : String? = null,
    val email : String? = null,
    val phone_no : String? = null,
    val dob : String? = null,
    val totalExams : String? = null,
    val avgPercentage : String? = null,
    val highestPercentage : String? = null,
    val imageUrl : String? = null,
    val subscribersCount : Int = 0,
    val fcmToken : String? = null
) : Serializable {
    constructor() : this(
        name = null,
        name_lowercase = null,
        email = null,
        phone_no = null,
        dob = null,
        imageUrl = null,
        subscribersCount = 0,
        fcmToken = null
    )
}