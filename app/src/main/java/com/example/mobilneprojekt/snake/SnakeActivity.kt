package com.example.mobilneprojekt.snake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.mobilneprojekt.ui.theme.MobilneProjektTheme

class SnakeActivity : ComponentActivity() {
    private val myViewModel: SnakeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myViewModel.snakeEngine = SnakeEngine(
            scope = lifecycleScope,
            onGameEnded = { TODO() },
            onFoodEaten = { TODO() }
        )
        setContent {
            MobilneProjektTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SnakeGame()
                }
            }
        }
    }
    @Composable
    fun SnakeGame() {
        val state = myViewModel.snakeEngine!!.state.collectAsState(initial = null)
        Column {
            state.value?.let { Board(it) }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobilneProjektTheme {
        MobilneProjektTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
            }
        }
    }
}