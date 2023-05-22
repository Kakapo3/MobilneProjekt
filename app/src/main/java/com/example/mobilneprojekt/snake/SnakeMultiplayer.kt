package com.example.mobilneprojekt.snake

import android.os.Message
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONException
import org.json.JSONObject
import java.util.logging.Logger
import kotlin.concurrent.thread

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeMultiplayer(snakeViewModel: SnakeViewModel, navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Snake multiplayer",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        val idState = remember {
            mutableStateOf("b")
        }
        val openDialog = remember { mutableStateOf(false) }
        val dialogText = remember { mutableStateOf("") }
        Text(text = "Podaj id gracza",
            style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(5.dp))
        TextField(
            value = idState.value,
            onValueChange = {idState.value = it},
            textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(5.dp))
        val text = remember {
            mutableStateOf("Czekam...")
        }
        snakeButton(onClick = { hostMultiplayerGame(
            snakeViewModel,
            idState.value,
            navController,
            text,
            {(a,b) -> if (a || b) {
                navController.navigateUp()
                Logger.getLogger("SnakeMultiplayer").warning("Game ended")
                dialogText.value = if (b && !a) "Wygrałeś!" else if (!b) "Przegrałeś!" else "Remis!"
                openDialog.value = true
                true} else false},
            {dialogText.value = it; openDialog.value = true}
        ) }, text = "Start host")
        Spacer(modifier = Modifier.width(10.dp))
        snakeButton(onClick = { connectToMultiplayerGame(
            snakeViewModel,
            idState.value,
            navController,
            text,
            {(a,b) -> if (a || b) {
                navController.navigateUp()
                Logger.getLogger("SnakeMultiplayer").warning("Game ended")
                dialogText.value = if (b && !a) "Wygrałeś!" else if (!b) "Przegrałeś!" else "Remis!"
                openDialog.value = true
                true} else false},
            {dialogText.value = it; openDialog.value = true}) }, text = "Connect")
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = text.value,
            style = MaterialTheme.typography.bodyLarge)
        if (openDialog.value) {
            AlertDialog(onDismissRequest = { openDialog.value = false },
                text = { Text(text = dialogText.value) },
                confirmButton = {
                    snakeButton(onClick = { openDialog.value = false }, text = "Ok")
                })
        }
    }


}


fun hostMultiplayerGame(
    snakeViewModel: SnakeViewModel,
    opponentId: String,
    navController: NavController,
    text: MutableState<String>,
    onGameEnded: (Pair<Boolean, Boolean>) -> Boolean,
    onError: (String) -> Unit,
) {
    if (snakeViewModel.id.value == opponentId) {
        onError("Nie możesz grać sam ze sobą")
        return
    } else if (snakeViewModel.isWaitingForOpponent.value) {
        onError("Jesteś już w trakcie oczekiwania na przeciwnika")
        return
    }
    val firebase = FirebaseDatabase.getInstance("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")
    Logger.getLogger("SnakeMultiplayer").warning("hostMultiplayerGame")
    firebase.getReference("snake").setValue("snake")
    val myRef = firebase.getReference("snake").child("multiplayer").push()
    Logger.getLogger("SnakeMultiplayer").warning("myRef.key: ${myRef.key}")
    Logger.getLogger("SnakeMultiplayer").warning(".key: ${myRef.key}")
    val addGameToAccount = myRef.key?.let { firebase.getReference("snake").child("accounts").child(snakeViewModel.id.value).child("multiplayer").child(it) }
    Logger.getLogger("SnakeMultiplayer").warning("addGameToAccount: ${addGameToAccount.toString()}")
    addGameToAccount?.child("gameId")?.setValue(myRef.key)
    addGameToAccount?.child("sizeOfBoard")?.setValue(snakeViewModel.sizeOfBoard.value)
    addGameToAccount?.child("speedSnake")?.setValue(snakeViewModel.speedSnake.value)
    addGameToAccount?.child("secondPlayerId")?.setValue("0")
    var reading = false
    addGameToAccount?.child("secondPlayerId")?.addValueEventListener(object : ValueEventListener{
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
                        snake1 = listOf(listOf(1,1)),
                        food = Pair(8, 8).toList(),
                        direction = Direction.RIGHT,
                        isGameOver = false,
                        score = 0
                    )),
                MutableStateFlow(
                    SnakeState(
                        snake1 = listOf(listOf(5,5)),
                        food = Pair(8, 8).toList(),
                        direction = Direction.RIGHT,
                        isGameOver = false,
                        score = 0
                    )),
                onGameEnded,
                {Logger.getLogger("SnakeMultiplayer").warning("Eated food")},
                onError,
                snakeViewModel.id.value,
                snakeViewModel.id.value,
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
        notificationBody.put("message", "Gracz ${snakeViewModel.id.value} zaprasza Ciebie do gry!")   //Enter your notification message
        notificationBody.put("type", "Snake")
        notificationBody.put("id_opponent", snakeViewModel.id.value)
        notificationBody.put("id", opponentId)
        notification.put("to", topic)
        notification.put("data", notificationBody)
        Log.e("TAG", "try")
    } catch (e: JSONException) {
        Log.e("TAG", "onCreate: " + e.message)
    }

    sendNotification(notification, snakeViewModel)
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
            val ref = firebase.getReference("snake").child("accounts").child(opponentId).child("multiplayer").child(gameId)
            val boardSizeTask = ref.child("sizeOfBoard").get()
            Tasks.await(boardSizeTask)
            val boardSize = (Tasks.await(boardSizeTask).value as Long).toInt()
            val speedSnakeTask = ref.child("speedSnake").get()
            val speedSnake = Tasks.await(speedSnakeTask).value as Long
            Logger.getLogger("SnakeMultiplayer").warning("boardSize: $boardSize")
            Logger.getLogger("SnakeMultiplayer").warning("speedSnake: $speedSnake")
            ref.child("secondPlayerId").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snakeViewModel.snakeEngine = SnakeEngine(
                        snakeViewModel,
                        MutableStateFlow(
                            SnakeState(
                                snake1 = listOf(listOf(5,5)),
                                food = Pair(8, 8).toList(),
                                direction = Direction.RIGHT,
                                isGameOver = false,
                                score = 0
                            )),
                        MutableStateFlow(
                            SnakeState(
                                snake1 = listOf(listOf(1,1)),
                                food = Pair(8, 8).toList(),
                                direction = Direction.RIGHT,
                                isGameOver = false,
                                score = 0
                            )),
                        onGameEnded,
                        {Logger.getLogger("SnakeMultiplayer").warning("Eated food")},
                        onError,
                        opponentId,
                        snakeViewModel.id.value,
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
                    Logger.getLogger("SnakeMultiplayer").warning("Error in connectToMultiplayerGame")
                }
            })
            ref.child("secondPlayerId").setValue(snakeViewModel.id.value)
        }
    } catch (e: Exception) {
        Logger.getLogger("SnakeMultiplayer").warning("Error in connectToMultiplayerGame")
        onError("Błąd połączenia: ${e.message}")
    }
}

fun sendNotification(notification: JSONObject, snakeViewModel: SnakeViewModel) {
    Log.e("TAG", "sendNotification")
    val jsonObjectRequest = object : JsonObjectRequest(snakeViewModel.FCM_API, notification,
        Response.Listener<JSONObject> { response ->
            Log.i("TAG", "onResponse: $response")
        },
        Response.ErrorListener {
            Log.i("TAG", "onErrorResponse: Didn't work")
        }) {

        override fun getHeaders(): Map<String, String> {
            val params = HashMap<String, String>()
            params["Authorization"] = snakeViewModel.serverKey
            params["Content-Type"] = snakeViewModel.contentType
            return params
        }
    }
    snakeViewModel.requestQueue.add(jsonObjectRequest)
}
