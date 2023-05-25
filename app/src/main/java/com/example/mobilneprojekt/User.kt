package com.example.mobilneprojekt

import coil.request.ImageRequest

data class User(
    val uid: String,
    val name: String,
    val request: ImageRequest,
)