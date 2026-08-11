package com.example.testmaster.model

import java.io.Serializable

data class Subscriber(
    val uid: String? = null,
    val name: String? = null,
    val imageUrl: String? = null
) : Serializable
