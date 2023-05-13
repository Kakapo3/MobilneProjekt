package com.example.mobilneprojekt.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun Controller(onDirectionChange: (Int) -> Unit) {

    val buttonSize = Modifier.size(64.dp)
    val currentDirection = remember { mutableStateOf(Direction.RIGHT) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        AppIconButton(
            onClick = {
                if (currentDirection.value != Direction.DOWN) {
                    onDirectionChange.invoke(Direction.UP)
                    currentDirection.value = Direction.UP
                }
            },
            modifier = buttonSize,
            icon = Icons.Default.KeyboardArrowUp
        )
        Row {
            AppIconButton(icon = Icons.Default.KeyboardArrowLeft, modifier = buttonSize) {
                if (currentDirection.value != Direction.RIGHT) {
                    onDirectionChange.invoke(Direction.LEFT)
                    currentDirection.value = Direction.LEFT
                }
            }
            Spacer(modifier = buttonSize)
            AppIconButton(icon = Icons.Default.KeyboardArrowRight, modifier = buttonSize) {
                if (currentDirection.value != Direction.LEFT) {
                    onDirectionChange.invoke(Direction.RIGHT)
                    currentDirection.value = Direction.RIGHT
                }
            }
        }
        AppIconButton(icon = Icons.Default.KeyboardArrowDown, modifier = buttonSize) {
            if (currentDirection.value != Direction.DOWN) {
                onDirectionChange.invoke(Direction.DOWN)
                currentDirection.value = Direction.DOWN
            }
        }

    }
}

@Composable
fun AppIconButton(modifier: Modifier, icon: ImageVector, onClick: () -> Unit, ) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(64.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}