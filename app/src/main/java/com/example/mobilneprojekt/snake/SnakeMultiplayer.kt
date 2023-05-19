package com.example.mobilneprojekt.snake

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.logging.Logger

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
        Row{
            snakeButton(onClick = { hostMultiplayerGame(snakeViewModel, idState.value, navController, text) }, text = "Start host")
            Spacer(modifier = Modifier.width(10.dp))
            snakeButton(onClick = { connectToMultiplayerGame(snakeViewModel, idState.value, navController, text) }, text = "Connect")
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = text.value,
            style = MaterialTheme.typography.bodyLarge)



    }


}

fun hostMultiplayerGame(
    snakeViewModel: SnakeViewModel,
    opponentId: String,
    navController: NavController,
    text: MutableState<String>
) {
    val firebase = Firebase.database
    val myRef = firebase.getReference("snake").child("multiplayer").push()
    val addGameToAccount =
        myRef.key?.let { firebase.getReference("snake").child("accounts").child(snakeViewModel.id.toString()).child(it) }
    addGameToAccount?.child("gameId")?.setValue(myRef.key)
    addGameToAccount?.child("sizeOfBoard")?.setValue(snakeViewModel.sizeOfBoard.value)
    addGameToAccount?.child("speedSnake")?.setValue(snakeViewModel.speedSnake.value)
    addGameToAccount?.child("secondPlayerId")?.setValue("0")
    addGameToAccount?.child("secondPlayerId")?.addValueEventListener(object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            snakeViewModel.snakeEngine = SnakeEngine(
                snakeViewModel.viewModelScope,
                snakeViewModel,
                {(a,b) -> if (a || b) {navController.navigateUp(); true} else false},
                {Logger.getLogger("SnakeMultiplayer").warning("Eated food")},
                snakeViewModel.id.value,
                opponentId,
                snapshot.value.toString(),
                myRef
            )
                    navController.navigate("game")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    })
    text.value = "Czekam na gracza"
}

fun connectToMultiplayerGame(
    snakeViewModel: SnakeViewModel,
    opponentId: String,
    navController: NavController,
    text: MutableState<String>
) {
    val firebase = Firebase.database
    firebase.getReference("snake").child("accounts").child(opponentId).get().result.children.forEach {
        if (it.key == "gameId") {
            val gameId = it.value.toString()
            val ref = firebase.getReference("snake").child("accounts").child(gameId).child(gameId)
            val boardSize = ref.child("sizeOfBoard").get().result.value as Int
            val speedSnake = ref.child("speedSnake").get().result.value as Long
            ref.child("secondPlayerId").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snakeViewModel.snakeEngine = SnakeEngine(
                        snakeViewModel.viewModelScope,
                        snakeViewModel,
                        {(a,b) -> if (a || b) {true} else false},
                        {Logger.getLogger("SnakeMultiplayer").warning("Eated food")},
                        opponentId,
                        snakeViewModel.id.value,
                        opponentId,
                        firebase.getReference("snake").child("multiplayer").child(gameId),
                        boardSize,
                        speedSnake
                    )
                    navController.navigate("game")

                }

                override fun onCancelled(error: DatabaseError) {
                    Logger.getLogger("SnakeMultiplayer").warning("Error in connectToMultiplayerGame")
                }

            })
            ref.child("secondPlayerId").setValue(snakeViewModel.id.value)
        }
    }

}