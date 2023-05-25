package com.example.mobilneprojekt.snake

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobilneprojekt.snake.theme.SnakeMobilneProjektTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.logging.Logger

class SnakeActivity : ComponentActivity() {
    private val snakeViewModel: SnakeViewModel by viewModels()
    private lateinit var scope: CoroutineScope
    @SuppressLint("SourceLockedOrientationActivity")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            SnakeMobilneProjektTheme(
                dynamicColor = false
            ) {
                (LocalContext.current as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val navController = rememberNavController()
                val navBackStateEntry = navController.currentBackStackEntryAsState()
                val firstVisible = remember {mutableStateOf(false)}
                Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                                Log.i("SnakeActivity", "xd: ${navBackStateEntry?.value}")
                                TopAppBar(
                                    title = { Text(text = "Snake") },
                                    colors = TopAppBarDefaults.smallTopAppBarColors(),

                                    navigationIcon = {
                                        Log.i("SnakeActivity", "xddddd: ${navBackStateEntry.value?.destination?.route}")
                                        AnimatedVisibility(
                                            visible = navBackStateEntry.value?.destination?.route != "menu" && firstVisible.value,
                                            enter = expandHorizontally() + fadeIn(),
                                            exit = shrinkHorizontally() + fadeOut()
                                            ) {
                                            IconButton(onClick = { navController.navigateUp() }) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowBack,
                                                    contentDescription = "Back"
                                                )
                                            }
                                        }
                                        LaunchedEffect(key1 = true){
                                            firstVisible.value = true
                                        }

                                    }

                                )

                        },
                    ) { padding ->
                        Box(
                            modifier = Modifier
                                .padding(padding)
                                .fillMaxSize()
                                .background(Color(0xFF003302))
                        ){
                            NavHost(navController = navController, startDestination = "menu"){
                                composable("menu") {
                                    SnakeMenu(
                                        onSingleplayerClick = {

                                            snakeViewModel.snakeEngine = SnakeEngine(
                                                mutableState = MutableStateFlow(
                                                    SnakeState(
                                                        snake1 = listOf(listOf(5,5)),
                                                        food = Pair(8, 8).toList(),
                                                        direction = Direction.RIGHT,
                                                        isGameOver = false,
                                                        score = 0
                                                    )),
                                                mutableStateOpponent = MutableStateFlow(
                                                    SnakeState(
                                                        snake1 = listOf(listOf(1,1)),
                                                        food = Pair(8, 8).toList(),
                                                        direction = Direction.RIGHT,
                                                        isGameOver = false,
                                                        score = 0
                                                    )),
                                                onGameEnded = {(a,b) -> if(a || b) {navController.navigate("menu"); true} else false},
                                                onFoodEaten = { Logger.getLogger("SnakeActivity").warning("Food eaten")},
                                                hostingPlayerId = "a",
                                                player1Id = "a",
                                                snakeViewModel = snakeViewModel,
                                                onError = {Logger.getLogger("SnakeMultiplayer").warning("Opponent died"); navController.navigateUp()},
                                            )
                                            navController.navigate("game")
                                            },
                                        onMultiplayerClick = { navController.navigate("multiplayer") },
                                        onSettingsClick = { navController.navigate("settings") }
                                    )
                                }
                                composable("game") {
                                    SnakeGame()
                                }
                                composable("multiplayer") {
                                    SnakeMultiplayer(snakeViewModel, navController)
                                }
                                composable("settings") {
                                    SnakeSettings(snakeViewModel, navController)
                                }
                            }

                            val openDialog = remember { mutableStateOf(false) }
                            val dialogText = remember { mutableStateOf("") }

                            LaunchedEffect(Unit){
                                try {
                                    Logger.getLogger("SnakeActivity").info("id: ${Firebase.auth.currentUser?.uid}")
                                    Logger.getLogger("SnakeActivity").info("intent: ${intent.extras?.getString("type")}")
                                    Logger.getLogger("SnakeActivity").info("intent opponent: ${intent.extras?.getString("id_opponent")}")
                                    if(intent.extras != null && intent.extras?.getString("type") == "Snake-Multiplayer"){
                                        connectToMultiplayerGame(snakeViewModel, intent.extras?.getString("id_opponent")!!, navController,
                                            mutableStateOf("coś"),
                                            {(a,b) -> if (a || b) {
                                                Logger.getLogger("SnakeActivityWin").info("a: $a, b: $b")
                                                navController.navigateUp()
                                                runOnUiThread {
                                                    dialogText.value = if (b && !a) "Wygrałeś!" else if (!b) "Przegrałeś!" else "Remis!"
                                                    openDialog.value = true
                                                }
                                                true} else false},
                                            {
                                                runOnUiThread{
                                                    dialogText.value = it; openDialog.value = true; navController.navigateUp()
                                                }
                                            })
                                    }
                                } catch (e: Exception) {
                                    Log.e("StartSnake", e.stackTraceToString())
                                    Toast.makeText(this@SnakeActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }

                            }

                            if (openDialog.value) {
                                AlertDialog(onDismissRequest = { openDialog.value = false },
                                    text = { Text(text = dialogText.value) },
                                    confirmButton = {
                                        snakeButton(onClick = { openDialog.value = false }, text = "Ok")
                                    })
                            }
                        }

                    }


            }
        }
    }
    @Composable
    fun SnakeGame() {
        scope = rememberCoroutineScope()
        LaunchedEffect(key1 = true){
            snakeViewModel.snakeEngine!!.scope = scope
            snakeViewModel.snakeEngine!!.runGame()
        }
        val state = snakeViewModel.snakeEngine!!.mutableStateExposed.collectAsState()
        val stateOpponent = if(snakeViewModel.snakeEngine!!.player2Id != null) snakeViewModel.snakeEngine!!.mutableStateOpponentExposed.collectAsState() else null
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            Board(state.value, stateOpponent?.value, snakeViewModel)
            Controller {
                when (it) {
                    Direction.UP -> {snakeViewModel.snakeEngine!!.move = Pair(0, -1); Log.i("SnakeActivity", "UP")}
                    Direction.LEFT -> {snakeViewModel.snakeEngine!!.move = Pair(-1, 0); Log.i("SnakeActivity", "LEFT")}
                    Direction.RIGHT -> {snakeViewModel.snakeEngine!!.move = Pair(1, 0); Log.i("SnakeActivity", "RIGHT")}
                    Direction.DOWN -> {snakeViewModel.snakeEngine!!.move = Pair(0, 1); Log.i("SnakeActivity", "DOWN")}
                }
            }
        }
    }
}
