package com.example.mobilneprojekt.snake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilneprojekt.ui.theme.MobilneProjektTheme
import kotlinx.coroutines.CoroutineScope
import java.util.logging.Logger

class SnakeActivity : ComponentActivity() {
    private val myViewModel: SnakeViewModel by viewModels()
    private lateinit var scope: CoroutineScope
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobilneProjektTheme(
                dynamicColor = false,
                darkTheme = true
            ) {
                    val navController = rememberNavController()
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                                SmallTopAppBar(
                                    title = { Text(text = "Snake") },
                                    colors = TopAppBarDefaults.smallTopAppBarColors(),
                                    navigationIcon = if (navController.previousBackStackEntry != null) { {
                                            IconButton(onClick = { navController.navigateUp() }) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowBack,
                                                    contentDescription = "Back"
                                                )
                                            }
                                        }
                                    } else {
                                        { }
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

                                            myViewModel.snakeEngine = SnakeEngine(
                                                scope = lifecycleScope,
                                                onGameEnded = {(a,b) -> if(a || b) {navController.navigate("menu"); true} else false},
                                                onFoodEaten = { Logger.getLogger("SnakeActivity").warning("Food eaten")},
                                                hostingPlayerId = "a",
                                                player1Id = "a"
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
                                    SnakeMultiplayer(myViewModel)
                                }
                                composable("settings") {
                                    SnakeSettings(myViewModel)
                                }
                            }
                        }

                    }


            }
        }
    }
    @Composable
    fun SnakeGame() {
        scope = rememberCoroutineScope()
        val state = myViewModel.snakeEngine!!.mutableStateExposed.collectAsState()
        val stateOpponent = myViewModel.snakeEngine!!.mutableStateOpponentExposed?.collectAsState()
        Column {
            Board(state.value, stateOpponent?.value)
            Controller {
                when (it) {
                    Direction.UP -> myViewModel.snakeEngine!!.move = Pair(0, -1)
                    Direction.LEFT -> myViewModel.snakeEngine!!.move = Pair(-1, 0)
                    Direction.RIGHT -> myViewModel.snakeEngine!!.move = Pair(1, 0)
                    Direction.DOWN -> myViewModel.snakeEngine!!.move = Pair(0, 1)
                }
            }
        }
    }
}
