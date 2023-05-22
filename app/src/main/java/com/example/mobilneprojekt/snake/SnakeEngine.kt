package com.example.mobilneprojekt.snake

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.EventListener
import java.util.logging.Logger
import kotlin.concurrent.thread
import kotlin.random.Random

class SnakeEngine(
    val snakeViewModel: SnakeViewModel,
    val mutableState: MutableStateFlow<SnakeState>,
    val mutableStateOpponent: MutableStateFlow<SnakeState>,
    private val onGameEnded: (Pair<Boolean,Boolean>) -> Boolean,
    private val onFoodEaten: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val hostingPlayerId: String,
    private val player1Id: String,
    val player2Id: String? = null,
    private val myGameRef: DatabaseReference? = null,
    private val boardSize: Int = snakeViewModel.sizeOfBoard.value,
    private val delay: Long = snakeViewModel.speedSnake.value
) {
    var scope: CoroutineScope? = null
    private val loose = Pair(false, false)
    private val currentDirection = mutableStateOf(Direction.RIGHT)
    private val opponentReadyTurn = if (myGameRef != null) mutableStateOf(0) else mutableStateOf(Int.MAX_VALUE)
    private var gameEnded = false
    private var currentTimeMillis = SystemClock.uptimeMillis()

    private val mutex = Mutex()
    private var round = 0

    var mutableStateOpponentExposed = MutableStateFlow(
        mutableStateOpponent.value)

    var mutableStateExposed = MutableStateFlow(mutableState.value)

    var dataOpponentStack = mutableListOf<SnakeState>()

    val valueListeners = mutableListOf<ValueEventListener>()
    val childListeners = mutableListOf<ChildEventListener>()

    var move = Pair(1,0)
        set(value) {
            scope?.launch {
                mutex.withLock{
                    field = value
                }
            }
        }

    init {
        snakeViewModel.currentSizeBoard.value = boardSize
        if(myGameRef != null){
            thread{
                val player1Ready = Tasks.await(myGameRef.child(player1Id).child("gameReady").get()).value ?: -1
                opponentReadyTurn.value = player1Ready as Int
            }
        }
        myGameRef?.child(player1Id)?.child("round")?.setValue(round)

        myGameRef?.child(player2Id!!)?.child("gameReady")?.addValueEventListener(object : ValueEventListener {
            init {
                valueListeners.add(this)
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                try{
                    Logger.getLogger("SnakeEngineReady").info("player2: $player2Id ready: ${snapshot.value}")
                    val value = snapshot.value
                    if (value == null) {
                        opponentReadyTurn.value = -1
                        return
                    }
                    opponentReadyTurn.value = (snapshot.value as Long).toInt()
                } catch (e: Exception){
                    onError("gameReady: ${e.message }")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })

        myGameRef?.child("foodPosition")?.addChildEventListener(object : ChildEventListener {
            init {
                childListeners.add(this)
            }
            fun test(snapshot: DataSnapshot) = thread {
                try{
                    Logger.getLogger("SnakeEngineFood").info("food: ${snapshot.value}")
                    val food = Tasks.await(myGameRef.child("foodPosition").get()).getValue(object : GenericTypeIndicator<List<Int>>() {})
                    mutableState.update { it.copy(food = food!!) }
                } catch (e: Exception){
                    onError("foodPosition: ${e.message }")
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                test(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                test(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                test(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                test(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })

        myGameRef?.child(player2Id!!)?.child("round")?.addValueEventListener(object : ValueEventListener {
            init {
                valueListeners.add(this)
            }
            fun test(snapshot: DataSnapshot) {
                try {
                    thread{
                        Logger.getLogger("SnakeEngineData").info("data: ${snapshot.value}")
                        val a = myGameRef.child(player2Id).child("data").get()
                        Tasks.await(a).getValue(SnakeState::class.java)?.let { state ->
                            dataOpponentStack.add(state)
                        }
                    }
                } catch (e: Exception) {
                    onError.invoke("data: ${e.message}, ${e.stackTraceToString()}")
                }
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                test(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })
//        runGame()
    }

    fun runGame() {
        scope?.launch {
            try {
                var snakeLength = 2
                var score = 0
                var gameStarted = false

                Logger.getLogger("SnakeEngine").info("runGame")
                Logger.getLogger("SnakeEngine").info("myGameRef: $myGameRef")
                Logger.getLogger("SnakeEngine").info("player1Id: $player1Id")
                myGameRef?.child(player1Id)?.child("gameReady")?.setValue(round)
                myGameRef?.child(player1Id)?.child("data")?.setValue(mutableState.value)
                Log.i("Snake", "Game started")
                currentTimeMillis = SystemClock.uptimeMillis()


                while (!gameEnded) {
                    Logger.getLogger("SnakeEngine").info("opp: ${opponentReadyTurn.value}, round: $round")
                    if ((SystemClock.uptimeMillis() - currentTimeMillis >= delay) && (opponentReadyTurn.value >= round)) {
                        gameStarted = true
                        Log.i("SnakeGameRunning", "Game running")
                        mutableState.update {
                            val hasReachedLeftEnd =
                                it.snake1.first()[0] == 0 && it.direction == Direction.LEFT
                            Logger.getLogger("SnakeEngineLoose").info("hasReachedLeftEnd: $hasReachedLeftEnd, direction: ${it.direction}, snake1: ${it.snake1.first()}")
                            val hasReachedTopEnd =
                                it.snake1.first()[1] == 0 && it.direction == Direction.UP
                            Logger.getLogger("SnakeEngineLoose").info("hasReachedTopEnd: $hasReachedTopEnd, direction: ${it.direction}, snake1: ${it.snake1.first()}")
                            val hasReachedRightEnd =
                                it.snake1.first()[0] == boardSize - 1 && it.direction == Direction.RIGHT
                            Logger.getLogger("SnakeEngineLoose").info("hasReachedRightEnd: $hasReachedRightEnd, direction: ${it.direction}, snake1: ${it.snake1.first()}")
                            val hasReachedBottomEnd =
                                it.snake1.first()[1] == boardSize - 1 && it.direction == Direction.DOWN
                            Logger.getLogger("SnakeEngineLoose").info("hasReachedBottomEnd: $hasReachedBottomEnd, direction: ${it.direction}, snake1: ${it.snake1.first()}")
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
                                        (poz[0] + move.first + boardSize) % boardSize,
                                        (poz[1] + move.second + boardSize) % boardSize
                                    )
                                }
                            }
                            val foodEaten = newPosition.toList() == it.food
                            val foodPosition = if (foodEaten) generateFoodPosition() else it.food
                            myGameRef?.child("foodPosition")?.setValue(foodPosition)
                            if (foodEaten) {
                                onFoodEaten.invoke(hostingPlayerId == player1Id)
                                snakeLength++
                                score++
                            }
                            loose = loose || it.snake1.contains(newPosition.toList())
                            Logger.getLogger("SnakeEngineLoose").info("snake1: ${it.snake1}")
                            Logger.getLogger("SnakeEngineLoose").info("newPosition: $newPosition")
                            Logger.getLogger("SnakeEngineLoose").info("contains: ${it.snake1.contains(newPosition.toList())}")
                            Log.i("SnakeEngineLoose", "loose: $loose")
                            it.copy(
                                food = foodPosition,
                                snake1 = listOf(newPosition.toList()) + it.snake1.take(snakeLength - 1),
                                direction = currentDirection.value,
                                isGameOver = loose,
                                score = score
                            )
                        }
                        Log.i("Snake", "Game updated")
                        Logger.getLogger("SnakeEngineDataTest").info("data: ${mutableState.value}")
                        Logger.getLogger("SnakeEngineDataTest").info("id: $player1Id")
                        myGameRef?.child(player1Id)?.child("data")?.setValue(mutableState.value)
                        round++
                        myGameRef?.child(player1Id)?.child("round")?.setValue(round)
                        if (myGameRef == null) {
                            gameEnded = onGameEnded.invoke(Pair(mutableState.value.isGameOver, false))
                        } else{
                            while (dataOpponentStack.isEmpty()) {
                                if (SystemClock.uptimeMillis() - currentTimeMillis >= 200 * delay && opponentReadyTurn.value < round){
                                    onError.invoke("Opponent left the game")
                                    gameEnded = true
                                }
                                delay(10)
                            }
                            mutableStateOpponent.value = dataOpponentStack.removeLast()
                            dataOpponentStack.clear()
                            val playerOne = mutableState.value
                            var loose = Pair(mutableState.value.isGameOver,
                                mutableStateOpponent.value.isGameOver
                            )
                            mutableStateOpponent.value.let { opponent ->
                                if (playerOne.snake1.contains(opponent.snake1.first()))
                                    loose = loose.copy(first = true)
                                if (opponent.snake1.contains(playerOne.snake1.first()))
                                    loose = loose.copy(second = true)
                                gameEnded = onGameEnded.invoke(loose)
                            }
                        }
                        updateStates()

                    }
                    if ((SystemClock.uptimeMillis() - currentTimeMillis >= 200 * delay && opponentReadyTurn.value < round && gameStarted)){
                        onError.invoke("Opponent left the game")
                        gameEnded = true
                    }
                    if(SystemClock.uptimeMillis() - currentTimeMillis >= 200 * delay && !gameStarted){
                        onError.invoke("Opponent left the game")
                        gameEnded = true
                    }
                    delay(10)
                }
            } catch (e: Exception) {
                onError.invoke("mainLoop: ${e.message }" ?: "Unknown error")
            } finally {
                for (listener in valueListeners)
                    myGameRef?.removeEventListener(listener)
                for (listener in childListeners)
                    myGameRef?.removeEventListener(listener)
            }
        }
    }

    private fun generateFoodPosition(): List<Int> {
        var a = Pair(Random.nextInt(0, boardSize), Random.nextInt(0, boardSize)).toList()
        while (mutableState.value.snake1.contains(a.toList()) || mutableStateOpponent.value.snake1.contains(a.toList()))
            a = Pair(Random.nextInt(0, boardSize), Random.nextInt(0, boardSize)).toList()
        return a
    }

    private fun updateStates() {
        mutableStateExposed.update { mutableState.value }
        mutableStateOpponentExposed.update { mutableStateOpponent.value }
        currentTimeMillis = SystemClock.uptimeMillis()
        Log.i("Snake", "States updated")
        Log.i("Snake", "Opponent ready: ${opponentReadyTurn.value}")
        Log.i("Snake", "Current time: ${SystemClock.uptimeMillis()}")
        Log.i("Snake", "Start time:   $currentTimeMillis")
        myGameRef?.child(player1Id)?.child("gameReady")?.setValue(round)
        Log.i("Snake", "Game essa")
    }


}