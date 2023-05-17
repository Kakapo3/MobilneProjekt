package com.example.mobilneprojekt.snake

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class SnakeEngine(
    val scope: CoroutineScope,
    val snakeViewModel: SnakeViewModel,
    private val onGameEnded: (Pair<Boolean,Boolean>) -> Boolean,
    private val onFoodEaten: (Boolean) -> Unit,
    private val hostingPlayerId: String,
    private val player1Id: String,
    private val player2Id: String? = null,
    private val myGameRef: DatabaseReference? = null,
) {
    private val loose = Pair(false, false)
    private val currentDirection = mutableStateOf(Direction.RIGHT)
    private val opponentReady = if (myGameRef != null) mutableStateOf(false) else mutableStateOf(true)
    private var gameEnded = false
    private var currentTimeMillis = SystemClock.uptimeMillis()

    val boardSize = snakeViewModel.sizeOfBoard.value
    val delay = snakeViewModel.speedSnake.value

    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(
        SnakeState(
            snake1 = listOf(Pair(5, 5)),
            food = Pair(8, 8),
            direction = Direction.RIGHT,
            isGameOver = false,
            score = 0
        )
    )

    private var mutableStateOpponent: MutableStateFlow<SnakeState>? = null

    var mutableStateOpponentExposed: MutableStateFlow<SnakeState>? = mutableStateOpponent?.let {
        MutableStateFlow(
            it.value)
    }

    var mutableStateExposed: MutableStateFlow<SnakeState> = MutableStateFlow(mutableState.value)

    var move = Pair(1,0)
        set(value) {
            scope.launch {
                mutex.withLock{
                    field = value
                }
            }
        }

    init {
        myGameRef?.child(player2Id!!)?.child("gameReady")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                opponentReady.value = snapshot.value as Boolean
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })

        myGameRef?.child("foodPosition")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val food = snapshot.getValue(Pair::class.java) as Pair<Int, Int>? ?: return
                mutableState.update { it.copy(food = food) }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })

        myGameRef?.child(player2Id!!)?.child("data")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(SnakeState::class.java)?.let { state ->
                    if (mutableStateOpponent == null)
                        mutableStateOpponent = MutableStateFlow(state)
                    else
                        mutableStateOpponent?.update { state }
                }
                val playerOne = mutableState.value
                var loose = Pair(mutableState.value.isGameOver, mutableStateOpponent?.value?.isGameOver ?: false)
                mutableStateOpponent?.value?.let { opponent ->
                    if (playerOne.snake1.contains(opponent.snake1.first()))
                        loose = loose.copy(first = true)
                    if (opponent.snake1.contains(playerOne.snake1.first()))
                        loose = loose.copy(second = true)
                    gameEnded = onGameEnded.invoke(loose)
                    if (playerOne.food == opponent.snake1.first()) {
                        onFoodEaten.invoke(hostingPlayerId == player2Id)
                        if(hostingPlayerId == player2Id)
                            myGameRef.child("foodPosition").setValue(generateFoodPosition())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })

        myGameRef?.child("foodPosition")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val food = snapshot.getValue(Pair::class.java) as Pair<Int, Int>? ?: return
                mutableState.update { it.copy(food = food) }
                updateStates()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })

        runGame()
    }

    fun runGame() {
        scope.launch {
            var snakeLength = 2
            var score = 0


            myGameRef?.child(player1Id)?.child("gameReady")?.setValue(true)
            myGameRef?.child(player1Id)?.child("data")?.setValue(mutableState)
            Log.i("Snake", "Game started")
            currentTimeMillis = SystemClock.uptimeMillis()
            while (!gameEnded) {
                Log.i("Snake", "Game running: ${!gameEnded}")
                Log.i("Snake", "Opponent ready: ${opponentReady.value}")
                Log.i("Snake", "Current time: ${SystemClock.uptimeMillis()}")
                Log.i("Snake", "Start time:   $currentTimeMillis")
                if ((SystemClock.uptimeMillis() - currentTimeMillis >= delay && opponentReady.value)) {
                    Log.i("Snake", "Game running")

                    opponentReady.value = myGameRef == null
                    mutableState.update {
                        val hasReachedLeftEnd =
                            it.snake1.first().first == 0 && it.direction == Direction.LEFT
                        val hasReachedTopEnd =
                            it.snake1.first().second == 0 && it.direction == Direction.UP
                        val hasReachedRightEnd =
                            it.snake1.first().first == boardSize - 1 && it.direction == Direction.RIGHT
                        val hasReachedBottomEnd =
                            it.snake1.first().second == boardSize - 1 && it.direction == Direction.DOWN
                        var loose =
                            hasReachedLeftEnd || hasReachedTopEnd || hasReachedRightEnd || hasReachedBottomEnd
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
                                    (poz.first + move.first + boardSize) % boardSize,
                                    (poz.second + move.second + boardSize) % boardSize
                                )
                            }
                        }
                        val foodEaten = newPosition == it.food
                        if (foodEaten) {
                            onFoodEaten.invoke(hostingPlayerId == player1Id)
                            snakeLength++
                            score++
                        }
                        loose = loose || it.snake1.contains(newPosition)
                        it.copy(
                            food = if (myGameRef == null && foodEaten) generateFoodPosition() else it.food,
                            snake1 = listOf(newPosition) + it.snake1.take(snakeLength - 1),
                            direction = currentDirection.value,
                            isGameOver = loose,
                            score = score
                        )
                    }
                    Log.i("Snake", "Game updated")
                    myGameRef?.child(player1Id)?.child("data")?.setValue(mutableState.value)
                    if (myGameRef == null) {
                        gameEnded = onGameEnded.invoke(loose)
                        updateStates()
                    }
                    Log.i("Snake", "Game essa")
                }
                delay(10)
            }
        }
    }

    private fun generateFoodPosition(): Pair<Int, Int> {
        var a = Pair(Random.nextInt(0, boardSize), Random.nextInt(0, boardSize))
        while (mutableState.value.snake1.contains(a) || mutableStateOpponent?.value?.snake1?.contains(a) == true)
            a = Pair(Random.nextInt(0, boardSize), Random.nextInt(0, boardSize))
        return a
    }

    private fun updateStates() {
        mutableStateExposed.update { mutableState.value }
        mutableStateOpponentExposed?.update { mutableStateOpponent?.value!! }
        currentTimeMillis = SystemClock.uptimeMillis()
        Log.i("Snake", "States updated")
        Log.i("Snake", "Opponent ready: ${opponentReady.value}")
        Log.i("Snake", "Current time: ${SystemClock.uptimeMillis()}")
        Log.i("Snake", "Start time:   $currentTimeMillis")
        myGameRef?.child(player1Id)?.child("gameReady")?.setValue(true)
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


}