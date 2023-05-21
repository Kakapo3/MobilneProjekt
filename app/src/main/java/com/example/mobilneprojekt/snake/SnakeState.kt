package com.example.mobilneprojekt.snake
import kotlinx.serialization.Serializable
object Direction {
    const val UP = 1
    const val DOWN = 2
    const val LEFT = 3
    const val RIGHT = 4
}

@Serializable
data class SnakeState(
    var snake1: List<List<Int>> = listOf(),
    var food: List<Int> = Pair(0, 0).toList(),
    var direction: Int = Direction.RIGHT,
    var isGameOver: Boolean = false,
    var score: Int = 0
)