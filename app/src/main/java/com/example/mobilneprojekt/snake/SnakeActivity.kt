package com.example.mobilneprojekt.snake

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobilneprojekt.ui.theme.MobilneProjektTheme
import kotlinx.coroutines.CoroutineScope
import java.util.logging.Logger

class SnakeActivity : ComponentActivity() {
    private val myViewModel: SnakeViewModel by viewModels()
    private lateinit var scope: CoroutineScope
    @SuppressLint("SourceLockedOrientationActivity")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobilneProjektTheme(
                dynamicColor = false,
                darkTheme = true
            ) {
                (LocalContext.current as? Activity)?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

                                            myViewModel.snakeEngine = SnakeEngine(
                                                scope = lifecycleScope,
                                                onGameEnded = {(a,b) -> if(a || b) {navController.navigate("menu"); true} else false},
                                                onFoodEaten = { Logger.getLogger("SnakeActivity").warning("Food eaten")},
                                                hostingPlayerId = "a",
                                                player1Id = "a",
                                                snakeViewModel = myViewModel
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
                                    SnakeSettings(myViewModel, navController)
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
            Board(state.value, stateOpponent?.value, myViewModel)
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
