package com.example.mobilneprojekt.snake

object Direction {
    const val UP = 1
    const val DOWN = 2
    const val LEFT = 3
    const val RIGHT = 4
}

data class SnakeState(
    val snake1: List<Pair<Int, Int>>,
    val food: Pair<Int, Int>,
    val direction: Int,
    val isGameOver: Boolean,
    val score: Int
)