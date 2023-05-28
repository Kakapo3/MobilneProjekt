package com.example.mobilneprojekt.firebase

import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mobilneprojekt.snake.SnakeViewModel
import org.json.JSONException
import org.json.JSONObject

class FirebaseMessageSender(app: Context) {
    val FCM_API = "https://fcm.googleapis.com/fcm/send"
    val serverKey =
        "key=" + "AAAAtssMyv4:APA91bHGxdS72jbXhPFGH72duHxt0kEgHfs-8cSCcxhr_sLhVyPtKYx_6Rt_9dOSN1B_YtcbI_amifPXjO6WZUXrjdbSogeNMTcB-BqW02CEb4M6XDNZ6xaYjWIW6CRIffLz5QogHBxr"
    val contentType = "application/json"
    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(app.applicationContext)
    }



    fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun sendNotification(
        to: String,
        title: String,
        message: String,
        type: String,
        params: Map<String, String>
    ){
        val notification = JSONObject()
        val notificationBody = JSONObject()
        try {
            notificationBody.put("title", title)
            notificationBody.put("message", message)   //Enter your notification message
            notificationBody.put("type", type)
            for (param in params) {
                notificationBody.put(param.key, param.value)
            }
            notification.put("to", to)
            notification.put("data", notificationBody)
            Log.e("TAG", "try")
            sendNotification(notification)
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }
    }
}