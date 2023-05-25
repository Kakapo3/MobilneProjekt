package com.example.mobilneprojekt.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.snake.SnakeActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random
import java.util.logging.Logger

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        Logger.getLogger("FirebaseService").info("service created")
        Logger.getLogger("FirebaseService").info("user: ${Firebase.auth.currentUser?.uid}")
        Firebase.auth.addAuthStateListener {
            Logger.getLogger("FirebaseService").info("user: ${Firebase.auth.currentUser?.uid}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val a = FirebaseApp.getInstance()
        Logger.getLogger("FirebaseToken").info("token: $token")

    }

    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Logger.getLogger("FirebaseMessage").info("message: ${p0.data}")

        val intent = when (p0.data["type"]) {
            "Snake" -> {
                val a = Intent(this, SnakeActivity::class.java)
                a.putExtras(
                    Bundle().apply {
                        putString("id_opponent", p0.data["id_opponent"])
                        putString("id", p0.data["id"])
                        putString("type", "Snake-Multiplayer")
                    }
                )
                a
            }
            else -> Intent(this, MainActivity::class.java)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them.
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setContentTitle(p0.data["title"])
            .setContentText(p0.data["message"])
            .setSmallIcon(this.resources.getIdentifier("ic_launcher", "mipmap", this.packageName))
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)
            .setChannelId(ADMIN_CHANNEL_ID)

        //Set notification color to match your app color template
        notificationManager.notify(notificationID, notificationBuilder.build())
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