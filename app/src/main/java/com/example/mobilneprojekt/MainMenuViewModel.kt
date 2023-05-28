package com.example.mobilneprojekt

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import coil.request.ImageRequest
import com.example.mobilneprojekt.firebase.FirebaseMessageSender
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random
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

    val achievements = mutableStateListOf<String>()
    val achievementList = HashMap<String, Achievement>()
    val games = listOf("arkanoid", "snake", "minesweeper")
    private val ADMIN_CHANNEL_ID = "admin_channel"

    private var maxCount = 20
    init {

        for (game in games) {
            db.getReference("${game}/achievements").get().addOnSuccessListener { data ->
                data.children.forEach { a ->
                    achievementList.put(a.key.toString(), Achievement(
                        game = game,
                        title = a.child("title").getValue(String::class.java) ?: "",
                        description = a.child("description").getValue(String::class.java) ?: ""))
                }

                Logger.getLogger("Achievemet").info(achievementList.toString())
            }
        }

        val achievementListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                achievements.add(snapshot.key.toString())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.getLogger("Achievemet").info("Changed ${snapshot.key.toString()}")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.getLogger("Achievemet").info("Removed ${snapshot.key.toString()}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.getLogger("Achievemet").info("Moved ${snapshot.key.toString()}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        val accountsListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                thread {
                    Logger.getLogger("infoListenerFriends").warning("onChildAdded: ${snapshot.key}")
                    val uid = snapshot.key ?: ""
                    if (uid != auth.currentUser?.uid && snapshot.getValue(Boolean::class.java) == true) {
                        accountsList.add(User(uid, getName(uid), getImageRequest(uid, app.applicationContext)))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val uid = snapshot.key ?: ""
                if (uid != auth.currentUser?.uid && snapshot.getValue(Boolean::class.java) == true) {
                    accountsList.add(User(uid, getName(uid), getImageRequest(uid, app.applicationContext)))
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
                    friendsList.add(User(uid, getName(uid), getImageRequest(uid, app.applicationContext)))
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
                    invites.add(User(uid, getName(uid), getImageRequest(uid, app.applicationContext)))
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
        val achievmentListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.getLogger("Achievemet").info("Added ${snapshot.key.toString()}")
                val key = snapshot.key.toString()

                val notificationManager = app.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationID = Random().nextInt(3000)

                /*
                Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
                to at least one of them.
                */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setupChannels(notificationManager)
                }

                val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(app.applicationContext, ADMIN_CHANNEL_ID)
                    .setContentTitle(achievementList[key]?.title)
                    .setContentText(achievementList[key]?.description)
                    .setSmallIcon(app.applicationContext.resources.getIdentifier("ic_launcher", "mipmap", app.applicationContext.packageName))
                    .setAutoCancel(true)
                    .setSound(notificationSoundUri)
                    .setChannelId(ADMIN_CHANNEL_ID)

                //Set notification color to match your app color template
                notificationManager.notify(notificationID, notificationBuilder.build())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.getLogger("FirebaseService").info("changed child")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.getLogger("FirebaseService").info("removed child")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/achievements").removeEventListener(achievmentListener)
                Logger.getLogger("FirebaseService").info("removed child event listener")
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
                db.getReference("accounts/${Firebase.auth.currentUser?.uid}/achievements").addChildEventListener(achievementListener)
                Logger.getLogger("FirebaseService").info("added child event listener")
            }
            updateImageRequest(
                imageRequest = imageRequest,
                app = app.applicationContext,
            )
            updateUriItems()
        }
    }




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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to devie notification"

        val adminChannel =
            NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }
}