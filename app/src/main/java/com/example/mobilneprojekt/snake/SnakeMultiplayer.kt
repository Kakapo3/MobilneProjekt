package com.example.mobilneprojekt.snake

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mobilneprojekt.FriendsList
import java.util.logging.Logger

@OptIn(ExperimentalFoundationApi::class)
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
        val openDialog = remember { mutableStateOf(false) }
        val dialogText = remember { mutableStateOf("") }
        val text = remember {
            mutableStateOf("Czekam...")
        }
        val context = LocalContext.current
        FriendsList(
            list = snakeViewModel.friendsList) {
            snakeButton(onClick = { hostMultiplayerGame(
                snakeViewModel,
                it.uid,
                navController,
                text,
                {(a,b) -> if (a || b) {
                    navController.navigateUp()
                    Logger.getLogger("SnakeMultiplayer").warning("Game ended")
                    dialogText.value = if (b && !a) "Wygrałeś!" else if (!b) "Przegrałeś!" else "Remis!"
                    openDialog.value = true
                    true} else false},
                {dialogText.value = it; openDialog.value = true},
                context
            ) }, text = "Start host")
        }
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


