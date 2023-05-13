package com.example.mobilneprojekt.snake

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Random

class SnakeEngine(
    val scope: CoroutineScope,
    private val onGameEnded: () -> Unit,
    private val onFoodEaten: () -> Unit
) {

    private val currentDirection = mutableStateOf(Direction.RIGHT)

    init {
        scope.launch {
            var snakeLength = 2
            while (true){
                delay(150)
                mutableState.update {
                    val hasReachedLeftEnd =
                        it.snake1.first().first == 0 && it.direction == Direction.LEFT
                    val hasReachedTopEnd =
                        it.snake1.first().second == 0 && it.direction == Direction.UP
                    val hasReachedRightEnd =
                        it.snake1.first().first == BOARD_SIZE - 1 && it.direction == Direction.RIGHT
                    val hasReachedBottomEnd =
                        it.snake1.first().second == BOARD_SIZE - 1 && it.direction == Direction.DOWN
                    if (hasReachedLeftEnd || hasReachedTopEnd || hasReachedRightEnd || hasReachedBottomEnd) {
                        snakeLength = 2
                        onGameEnded.invoke()
                    }
                    if (move.first == 0 && move.second == -1) {
                        currentDirection.value = Direction.UP
                    } else if (move.first == -1 && move.second == 0) {
                        currentDirection.value = Direction.LEFT
                    } else if (move.first == 1 && move.second == 0) {
                        currentDirection.value = Direction.RIGHT
                    } else if (move.first == 0 && move.second == 1) {
                        currentDirection.value = Direction.DOWN
                    }

                    val newPosition = it.snake1.first().let { poz ->
                        mutex.withLock {
                            Pair(
                                (poz.first + move.first + BOARD_SIZE) % BOARD_SIZE,
                                (poz.second + move.second + BOARD_SIZE) % BOARD_SIZE
                            )
                        }
                    }

                    if (newPosition == it.food) {
                        onFoodEaten.invoke()
                        snakeLength++
                    }

                    if (it.snake1.contains(newPosition)) {
                        snakeLength = 2
                        onGameEnded.invoke()
                    }

                    it.copy(
                        food = if (newPosition == it.food) Pair(
                            Random().nextInt(BOARD_SIZE),
                            Random().nextInt(BOARD_SIZE)
                        ) else it.food,
                        snake1 = listOf(newPosition) + it.snake1.take(snakeLength - 1),
                        direction = currentDirection.value,
                    )
                }
            }
        }
    }

    fun reset() {
        mutableState.update {
            it.copy(
                food = Pair(5, 5),
                snake1 = listOf(Pair(7, 7)),
                direction = Direction.RIGHT
            )
        }
        currentDirection.value = Direction.RIGHT
        move = Pair(1, 0)
    }

    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(
        SnakeState(
            snake1 = listOf(Pair(5, 5)),
            food = Pair(0, 0),
            direction = Direction.RIGHT,
            isGameOver = false,
            score = 0
        )
    )

    val state: Flow<SnakeState> = mutableState

    var move = Pair(1,0)
        set(value) {
            scope.launch {
                mutex.withLock{
                    field = value
                }
            }
        }

    companion object {
        const val BOARD_SIZE = 32
    }
}