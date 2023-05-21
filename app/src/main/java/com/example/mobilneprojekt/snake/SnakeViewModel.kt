package com.example.mobilneprojekt.snake

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SnakeViewModel(app: Application) : AndroidViewModel(app) {
    var snakeEngine: SnakeEngine? = null
    var speedSnake: MutableStateFlow<Long>
    var sizeOfBoard: MutableStateFlow<Int>
    var id: MutableStateFlow<String>
    val currentSizeBoard: MutableStateFlow<Int>
    val FCM_API = "https://fcm.googleapis.com/fcm/send"
    val serverKey =
        "key=" + "AIzaSyCK5m00Ymwb8ZKW1ksDc9dm5IClu8J-ajc"
    val contentType = "application/json"
    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(app.applicationContext)
    }

    init{
        val sharedPrefs = getApplication<Application>().getSharedPreferences("settings-snake", Context.MODE_PRIVATE)
        sharedPrefs.apply {
            speedSnake = MutableStateFlow(getLong("speed", 200L))
            sizeOfBoard = MutableStateFlow(getInt("size", 15))
            id = MutableStateFlow(getString("id", "a")!!)
        }
        currentSizeBoard = MutableStateFlow(sizeOfBoard.value)

    }

    fun setSpeedSnake(){
        viewModelScope.launch {
            getApplication<Application>().getSharedPreferences("settings-snake", Context.MODE_PRIVATE).edit().apply {
                putLong("speed", speedSnake.value)
                apply()
            }
        }
    }

    fun setSizeOfBoard(){
        viewModelScope.launch {
            getApplication<Application>().getSharedPreferences("settings-snake", Context.MODE_PRIVATE).edit().apply {
                putInt("size", sizeOfBoard.value)
                apply()
            }
        }
    }

    fun setId(){
        viewModelScope.launch {
            getApplication<Application>().getSharedPreferences("settings-snake", Context.MODE_PRIVATE).edit().apply {
                putString("id", id.value)
                apply()
            }
        }
    }

    override fun onCleared() {
        snakeEngine!!.scope?.coroutineContext?.cancelChildren()
    }
}