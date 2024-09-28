package com.example.testmaster.model

import android.net.Uri
import java.io.Serializable

class personalDetail(
    val name : String? = null,
    val email : String? = null,
    val phone_no : String? = null,
    val dob : String? = null,
    val imageUrl : String? = null
) : Serializable {
    constructor() : this(
        name = null,
        email = null,
        phone_no = null,
        dob = null,
        imageUrl = null,
    )
}