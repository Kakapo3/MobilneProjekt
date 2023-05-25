package com.example.mobilneprojekt

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import coil.request.ImageRequest
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.concurrent.thread

fun getImageRequest(id: String, app: Context): ImageRequest {
    val db =
        Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    val res =
        Tasks.await(db.getReference("accounts/$id/avatar").get()).getValue(String::class.java)
    if (res != null) {
        val url = Tasks.await(Firebase.storage.getReference("$id/$res").downloadUrl)
        val newImageRequest = ImageRequest.Builder(app.applicationContext)
            .data(url)
            .build()
        return newImageRequest
    } else {
        val url = Tasks.await(Firebase.storage.getReference("53254.png").downloadUrl)
        val newImageRequest = ImageRequest.Builder(app.applicationContext)
            .data(url)
            .memoryCacheKey(url.toString())
            .build()
        return newImageRequest

    }
}

fun updateImageRequest(uri: Uri? = null, imageRequest: MutableState<ImageRequest?>, app: Context) = thread {
    val db =
        Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    if (uri != null) {
        val newImageRequest = ImageRequest.Builder(app.applicationContext)
            .data(uri)
            .memoryCacheKey(uri.toString())
            .build()
        imageRequest.value = newImageRequest
        db.getReference("accounts/${Firebase.auth.currentUser?.uid}/avatar")
            .setValue("profilowe.jpg")
        Firebase.storage.reference.child("${Firebase.auth.currentUser?.uid}/profilowe.jpg")
            .putFile(uri)
    } else {
        val res =
            Tasks.await(db.getReference("accounts/${Firebase.auth.currentUser?.uid}/avatar").get())
                .getValue(String::class.java)
        if (res != null) {
            Firebase.storage.getReference("${Firebase.auth.currentUser?.uid}/$res").downloadUrl.addOnSuccessListener {
                val newImageRequest = ImageRequest.Builder(app.applicationContext)
                    .data(it)
                    .memoryCacheKey(it.toString())
                    .build()
                imageRequest.value = newImageRequest
            }
        } else {
            Firebase.storage.getReference("53254.png").downloadUrl.addOnSuccessListener {
                val newImageRequest = ImageRequest.Builder(app.applicationContext)
                    .data(it)
                    .memoryCacheKey(it.toString())
                    .build()
                imageRequest.value = newImageRequest
            }
        }
    }
}

fun updateName(newName: String, name: MutableState<String>){
    val db =
        Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").setValue(newName)
    name.value = newName
}





fun getName(id: String): String {
    val db =
        Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    return Tasks.await(db.getReference("accounts/$id/name").get()).getValue(String::class.java) ?: ""
}


