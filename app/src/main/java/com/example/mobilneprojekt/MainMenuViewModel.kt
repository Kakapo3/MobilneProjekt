package com.example.mobilneprojekt

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import coil.request.ImageRequest
import com.example.mobilneprojekt.firebase.FirebaseMessageSender
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.logging.Logger
import kotlin.concurrent.thread

class MainMenuViewModel(val app: Application) : AndroidViewModel(app) {
    val name = mutableStateOf("")
    val friendsList = mutableStateListOf<User>()
    val accountsList = mutableStateListOf<User>()
    val imageRequest : MutableState<ImageRequest?> = mutableStateOf(null)
    val invites = mutableStateListOf<User>()
    val search = MutableStateFlow(false)

    val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    val auth = Firebase.auth

    private val _uriItems = MutableStateFlow(listOf<UriItem>())
    val uriItems = _uriItems.asStateFlow()
    val sender = FirebaseMessageSender(app)

    private var maxCount = 20
    init {
        val accountsListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                thread {
                    Logger.getLogger("infoListenerFriends").warning("onChildAdded: ${snapshot.key}")
                    val uid = snapshot.key ?: ""
                    if (uid != auth.currentUser?.uid && snapshot.getValue(Boolean::class.java) == true) {
                        accountsList.add(User(uid, getName(uid), getImageRequest(uid)))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val uid = snapshot.key ?: ""
                if (uid != auth.currentUser?.uid && snapshot.getValue(Boolean::class.java) == true) {
                    accountsList.add(User(uid, getName(uid), getImageRequest(uid)))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                friendsList.removeAll { it.uid == snapshot.key }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        val listenerFriends = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                thread {
                    Logger.getLogger("infoListenerFriends").warning("onChildAdded: ${snapshot.key}")
                    val uid = snapshot.key ?: ""
                    friendsList.add(User(uid, getName(uid), getImageRequest(uid)))
                }
            }
            override fun onChildChanged(
                snapshot: DataSnapshot,
                previousChildName: String?
            ) {
                TODO("Not yet implemented")
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                friendsList.removeAll { it.uid == snapshot.key }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        val inviteListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                thread {
                    val uid = snapshot.key ?: ""
                    invites.add(User(uid, getName(uid), getImageRequest(uid)))
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("invites", "onChildChanged: ${snapshot.value}")
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                invites.removeAll { it.uid == snapshot.key }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("invites", "onChildMoved: ${snapshot.value}")
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("invites", "onCancelled: ${error.message}")
            }
        }
        if (auth.currentUser != null) {
            db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").get()
                .addOnSuccessListener {
                    name.value = it.getValue(String::class.java) ?: ""
                }
        }
        Firebase.auth.addAuthStateListener { auth ->
            if (auth.currentUser == null) {
                friendsList.removeAll { true }
                invites.removeAll { true }
                name.value = ""
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends")
                    .removeEventListener(listenerFriends)
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/invites")
                    .removeEventListener(inviteListener)
                db.getReference("accounts_list").removeEventListener(accountsListener)
            } else {
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").get()
                    .addOnSuccessListener {
                        name.value = it.getValue(String::class.java) ?: ""
                    }
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends")
                    .addChildEventListener(listenerFriends)
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/invites")
                    .addChildEventListener(inviteListener)
                db.getReference("accounts_list").addChildEventListener(accountsListener)
            }
            updateImageRequest()
            updateUriItems()
        }
    }

    fun updateName(newName: String){
        db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").setValue(newName)
        name.value = newName
    }

    fun updateImageRequest(uri: Uri? = null) = thread {
        val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
        if (uri != null){
            val newImageRequest = ImageRequest.Builder(app.applicationContext)
                .data(uri)
                .memoryCacheKey(uri.toString())
                .build()
            imageRequest.value = newImageRequest
            db.getReference("accounts/${Firebase.auth.currentUser?.uid}/avatar").setValue("profilowe.jpg")
            Firebase.storage.reference.child("${Firebase.auth.currentUser?.uid}/profilowe.jpg").putFile(uri)
        } else {
            val res = Tasks.await(db.getReference("accounts/${Firebase.auth.currentUser?.uid}/avatar").get()).getValue(String::class.java)
            if(res != null){
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

    fun getImageRequest(id: String): ImageRequest {
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

    fun getName(id: String) = Tasks.await(db.getReference("accounts/$id/name").get()).getValue(String::class.java) ?: ""

    fun acceptInvite(uid: String){
        db.getReference("accounts/${Firebase.auth.currentUser?.uid}/invites/$uid").removeValue()
        db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends/$uid").setValue(1)
        db.getReference("accounts/$uid/friends/${Firebase.auth.currentUser?.uid}").setValue(1)
    }

    fun updateUriItems(){
        @SuppressLint("ObsoleteSdkInt")
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val contentResolver: ContentResolver = app.contentResolver
        contentResolver.query(
            collection,
            null,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor -> var count = 0
            val test = mutableListOf<UriItem>()
            while(cursor.moveToNext() && count < maxCount){
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                )
                test.add(UriItem(count, "Item 1", contentUri))
                count++
            }
            _uriItems.value = test
            maxCount += 1
        }
    }

    fun sendInvite(uid: String) {
        db.getReference("accounts/$uid/invites/${Firebase.auth.currentUser?.uid}").setValue(1)
        sender.sendNotification(
            to = "/topics/$uid",
            title = "Zaproszenie",
            message = "Zaproszenie do znajomych od ${name.value}",
            type = "invite",
            params = mapOf()
        )
    }
}