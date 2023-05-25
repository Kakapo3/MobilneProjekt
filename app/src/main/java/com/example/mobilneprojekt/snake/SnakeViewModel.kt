package com.example.mobilneprojekt.snake

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.mobilneprojekt.User
import com.example.mobilneprojekt.getImageRequest
import com.example.mobilneprojekt.getName
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.concurrent.thread

class SnakeViewModel(app: Application) : AndroidViewModel(app) {
    var snakeEngine: SnakeEngine? = null
    var speedSnake: MutableStateFlow<Long>
    var sizeOfBoard: MutableStateFlow<Int>
//    var id: MutableStateFlow<String>
    val currentSizeBoard: MutableStateFlow<Int>
    val friendsList = mutableStateListOf<User>()
    val name = mutableStateOf("")

    val isWaitingForOpponent = mutableStateOf(false)
    val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    init{
        val sharedPrefs = getApplication<Application>().getSharedPreferences("settings-snake", Context.MODE_PRIVATE)
        sharedPrefs.apply {
            speedSnake = MutableStateFlow(getLong("speed", 200L))
            sizeOfBoard = MutableStateFlow(getInt("size", 15))
//            id = MutableStateFlow(getString("id", "a")!!)
        }
        currentSizeBoard = MutableStateFlow(sizeOfBoard.value)

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
        db.getReference("accounts/${Firebase.auth.currentUser?.uid}/friends")
            .addChildEventListener(listenerFriends)

        db.getReference("accounts/${Firebase.auth.currentUser?.uid}/name").get()
            .addOnSuccessListener {
                name.value = it.getValue(String::class.java) ?: ""
            }

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

//    fun setId(){
//        viewModelScope.launch {
//            getApplication<Application>().getSharedPreferences("settings-snake", Context.MODE_PRIVATE).edit().apply {
//                putString("id", id.value)
//                apply()
//            }
//        }
//    }

    override fun onCleared() {
        snakeEngine?.scope?.coroutineContext?.cancelChildren()
    }
}