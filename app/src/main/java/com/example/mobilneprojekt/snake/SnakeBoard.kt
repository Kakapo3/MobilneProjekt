package com.example.mobilneprojekt.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mobilneprojekt.R

@Composable
fun Board(state: SnakeState, stateOpponent: SnakeState?, snakeViewModel: SnakeViewModel) {
    BoxWithConstraints(Modifier.padding(16.dp)) {
        val tileSize = maxWidth / snakeViewModel.currentSizeBoard.collectAsState().value
        Box(
            Modifier
                .size(maxWidth)
                .border(2.dp, Color.Gray))
        Box(
            Modifier
                .offset(x = tileSize * state.food[0], y = tileSize * state.food[1])
                .size(tileSize)
                .background(
                    Color.DarkGray, CircleShape
                )
        ){
            // show food icon
            Icon(Icons.Sharp.Star, contentDescription = "star", tint = Color.Yellow)
        }
        state.snake1.forEach {
            Box(
                modifier = Modifier
                    .offset(x = tileSize * it[0], y = tileSize * it[1])
                    .size(tileSize)
                    .background(
                        Color.DarkGray, RoundedCornerShape(4.dp)
                    )
            )
        }
        
        stateOpponent?.let {
            it.snake1.forEach { element ->
                Box(
                    modifier = Modifier
                        .offset(x = tileSize * element[0], y = tileSize * element[1])
                        .size(tileSize)
                        .background(
                            Color.Red, RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}