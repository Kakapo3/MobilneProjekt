package com.example.mobilneprojekt.snake

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import com.example.mobilneprojekt.firebase.FirebaseMessageSender
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONException
import org.json.JSONObject
import java.util.logging.Logger
import kotlin.concurrent.thread

fun hostMultiplayerGame(
    snakeViewModel: SnakeViewModel,
    opponentId: String,
    navController: NavController,
    text: MutableState<String>,
    onGameEnded: (Pair<Boolean, Boolean>) -> Boolean,
    onError: (String) -> Unit,
    context: Context
) {
    if (Firebase.auth.currentUser?.uid == opponentId) {
        onError("Nie możesz grać sam ze sobą")
        return
    } else if (snakeViewModel.isWaitingForOpponent.value) {
        onError("Jesteś już w trakcie oczekiwania na przeciwnika")
        return
    }
    val firebase =
        FirebaseDatabase.getInstance("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    Logger.getLogger("SnakeMultiplayer").warning("hostMultiplayerGame")
    firebase.getReference("snake").setValue("snake")
    val myRef = firebase.getReference("snake").child("multiplayer").push()
    Logger.getLogger("SnakeMultiplayer").warning("myRef.key: ${myRef.key}")
    Logger.getLogger("SnakeMultiplayer").warning(".key: ${myRef.key}")
    val addGameToAccount = myRef.key?.let { firebase.getReference("snake").child("accounts").child(
        Firebase.auth.currentUser?.uid!!).child("multiplayer").child(it) }
    Logger.getLogger("SnakeMultiplayer").warning("addGameToAccount: ${addGameToAccount.toString()}")
    addGameToAccount?.child("gameId")?.setValue(myRef.key)
    addGameToAccount?.child("sizeOfBoard")?.setValue(snakeViewModel.sizeOfBoard.value)
    addGameToAccount?.child("speedSnake")?.setValue(snakeViewModel.speedSnake.value)
    addGameToAccount?.child("secondPlayerId")?.setValue("0")
    var reading = false
    addGameToAccount?.child("secondPlayerId")?.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (!reading) {
                reading = true
                return
            }
            Logger.getLogger("SnakeMultiplayer").warning("Start game")
            snakeViewModel.snakeEngine = SnakeEngine(
                snakeViewModel,
                MutableStateFlow(
                    SnakeState(
                        snake1 = listOf(listOf(1, 1)),
                        food = Pair(8, 8).toList(),
                        direction = Direction.RIGHT,
                        isGameOver = false,
                        score = 0
                    )
                ),
                MutableStateFlow(
                    SnakeState(
                        snake1 = listOf(listOf(5, 5)),
                        food = Pair(8, 8).toList(),
                        direction = Direction.RIGHT,
                        isGameOver = false,
                        score = 0
                    )
                ),
                onGameEnded,
                {
                    Logger.getLogger("SnakeMultiplayer").warning("Eated food")
                    snakeViewModel.db.getReference("accounts/${Firebase.auth.currentUser?.uid}/achievements/snake1").setValue(true)
                },
                onError,
                Firebase.auth.currentUser?.uid!!,
                Firebase.auth.currentUser?.uid!!,
                snapshot.value.toString(),
                myRef
            )
            Logger.getLogger("SnakeMultiplayer").warning("navigating to game")
            addGameToAccount.child("secondPlayerId").removeEventListener(this)
            navController.navigate("game")
            snakeViewModel.isWaitingForOpponent.value = false
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    })

    val topic = "/topics/$opponentId" //topic has to match what the receiver subscribed to
    val notification = JSONObject()
    val notificationBody = JSONObject()
    try {
        notificationBody.put("title", "Snake")
        notificationBody.put("message", "Gracz ${snakeViewModel.name.value} zaprasza Ciebie do gry!")   //Enter your notification message
        notificationBody.put("type", "Snake")
        notificationBody.put("id_opponent", Firebase.auth.currentUser?.uid!!)
        notificationBody.put("id", opponentId)
        notification.put("to", topic)
        notification.put("data", notificationBody)
        Log.e("TAG", "try")
    } catch (e: JSONException) {
        Log.e("TAG", "onCreate: " + e.message)
    }

    val sender = FirebaseMessageSender(context)
    sender.sendNotification(notification)
    Logger.getLogger("SnakeMultiplayer").warning("send message")
}

fun connectToMultiplayerGame(
    snakeViewModel: SnakeViewModel,
    opponentId: String,
    navController: NavController,
    text: MutableState<String>,
    onGameEnded: (Pair<Boolean, Boolean>) -> Boolean,
    onError: (String) -> Unit
) {
    try {
        Logger.getLogger("SnakeMultiplayer").warning("connectToMultiplayerGame")
        val firebase = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
        val task = firebase.getReference("snake").child("accounts").child(opponentId).child("multiplayer").get()
        Logger.getLogger("SnakeMultiplayer").warning("task: $task, opponentId: $opponentId")
        thread {
            val result = Tasks.await(task)
            Logger.getLogger("SnakeMultiplayer").warning("result: ${result.value.toString()}")
            if (result.value == null) {
                onError("Nie ma takiego gracza")
                return@thread
            }
            val game = result.children.first()
            Logger.getLogger("SnakeMultiplayer").warning("it.key: ")
            val gameId = game.key!!
            Logger.getLogger("SnakeMultiplayer").warning("gameId: $gameId")
            val ref = firebase.getReference("snake").child("accounts").child(opponentId)
                .child("multiplayer").child(gameId)
            val boardSizeTask = ref.child("sizeOfBoard").get()
            Tasks.await(boardSizeTask)
            val boardSize = (Tasks.await(boardSizeTask).value as Long).toInt()
            val speedSnakeTask = ref.child("speedSnake").get()
            val speedSnake = Tasks.await(speedSnakeTask).value as Long
            Logger.getLogger("SnakeMultiplayer").warning("boardSize: $boardSize")
            Logger.getLogger("SnakeMultiplayer").warning("speedSnake: $speedSnake")
            ref.child("secondPlayerId").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snakeViewModel.snakeEngine = SnakeEngine(
                        snakeViewModel,
                        MutableStateFlow(
                            SnakeState(
                                snake1 = listOf(listOf(5, 5)),
                                food = Pair(8, 8).toList(),
                                direction = Direction.RIGHT,
                                isGameOver = false,
                                score = 0
                            )
                        ),
                        MutableStateFlow(
                            SnakeState(
                                snake1 = listOf(listOf(1, 1)),
                                food = Pair(8, 8).toList(),
                                direction = Direction.RIGHT,
                                isGameOver = false,
                                score = 0
                            )
                        ),
                        onGameEnded,
                        {
                            Logger.getLogger("SnakeMultiplayer").warning("Eated food")
                            snakeViewModel.db.getReference("accounts/${Firebase.auth.currentUser?.uid}/achievements/snake1").setValue(true)
                        },
                        onError,
                        opponentId,
                        Firebase.auth.currentUser?.uid!!,
                        opponentId,
                        firebase.getReference("snake").child("multiplayer").child(gameId),
                        boardSize,
                        speedSnake
                    )
                    Logger.getLogger("SnakeMultiplayer").warning("navigating to game")
                    ref.child("secondPlayerId").removeEventListener(this)
                    navController.navigate("game")
                }

                override fun onCancelled(error: DatabaseError) {
                    Logger.getLogger("SnakeMultiplayer")
                        .warning("Error in connectToMultiplayerGame")
                }
            })
            ref.child("secondPlayerId").setValue(Firebase.auth.currentUser?.uid!!)
        }
    } catch (e: Exception) {
        Logger.getLogger("SnakeMultiplayer").warning("Error in connectToMultiplayerGame")
        onError("Błąd połączenia: ${e.message}")
    }
}