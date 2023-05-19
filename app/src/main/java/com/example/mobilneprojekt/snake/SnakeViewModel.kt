package com.example.mobilneprojekt.snake

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SnakeViewModel(app: Application) : AndroidViewModel(app) {
    var snakeEngine: SnakeEngine? = null
    var speedSnake: MutableStateFlow<Long>
    var sizeOfBoard: MutableStateFlow<Int>
    var id: MutableStateFlow<String> = MutableStateFlow("a")


    init{
        var sharedPrefs = getApplication<Application>().getSharedPreferences("settings-snake", Context.MODE_PRIVATE)
        sharedPrefs.apply {
            speedSnake = MutableStateFlow(getLong("speed", 200L))
            sizeOfBoard = MutableStateFlow(getInt("size", 15))
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

    override fun onCleared() {
        snakeEngine!!.scope?.coroutineContext?.cancelChildren()
    }
}