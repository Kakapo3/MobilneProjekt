package com.example.mobilneprojekt.snake

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SnakeViewModel : ViewModel() {
    var snakeEngine: SnakeEngine? = null

    override fun onCleared() {
        snakeEngine!!.scope.coroutineContext.cancelChildren()
    }
}