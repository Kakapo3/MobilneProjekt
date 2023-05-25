package com.example.mobilneprojekt

import android.net.Uri

data class UriItem(
    val id: Int,
    val description: String,
    val uri: Uri,
    val stars: Float = 0f
)