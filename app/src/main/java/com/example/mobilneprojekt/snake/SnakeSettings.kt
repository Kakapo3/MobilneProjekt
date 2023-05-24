package com.example.mobilneprojekt.snake

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeSettings(snakeViewModel: SnakeViewModel, navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
    ){
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Speed",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(5.dp))
        Slider(
            value = snakeViewModel.speedSnake.collectAsState().value.toFloat(),
            onValueChange = {snakeViewModel.speedSnake.value = it.toLong()},
            valueRange = 200f..600f,
            steps = 3,
            onValueChangeFinished = {snakeViewModel.setSpeedSnake()}
        )
        Text(text = "${snakeViewModel.speedSnake.collectAsState().value}")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Speed",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(5.dp))
        Slider(
            value = snakeViewModel.sizeOfBoard.collectAsState().value.toFloat(),
            onValueChange = {snakeViewModel.sizeOfBoard.value = it.toInt()},
            valueRange = 10f..30f,
            steps = 18,
            onValueChangeFinished = {snakeViewModel.setSizeOfBoard()}
        )
        Text(text = "${snakeViewModel.sizeOfBoard.collectAsState().value}")
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "id",
            style = MaterialTheme.typography.bodyLarge,
        )
        TextField(
            value = snakeViewModel.id.collectAsState().value,
            onValueChange = {snakeViewModel.id.value = it; snakeViewModel.setId()},
        )
        Log.i("SnakeSettings", "nav: ${navController.currentBackStackEntry?.arguments}")

    }
}