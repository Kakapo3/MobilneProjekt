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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mobilneprojekt.R

@Composable
fun Board(state: SnakeState, stateOpponent: SnakeState?, snakeViewModel: SnakeViewModel) {
    BoxWithConstraints(Modifier.padding(16.dp)) {
        val tileSize = maxWidth / snakeViewModel.sizeOfBoard.collectAsState().value
        Box(
            Modifier
                .size(maxWidth)
                .border(2.dp, Color.Gray))
        Box(
            Modifier
                .offset(x = tileSize * state.food.first, y = tileSize * state.food.second)
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
                    .offset(x = tileSize * it.first, y = tileSize * it.second)
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
                        .offset(x = tileSize * element.first, y = tileSize * element.second)
                        .size(tileSize)
                        .background(
                            Color.Red, RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}