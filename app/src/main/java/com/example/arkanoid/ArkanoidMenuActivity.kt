package com.example.arkanoid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.arkanoid.ui.theme.ArkanoidComposeTheme
import com.example.arkanoidcompose.R

class ArkanoidMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ArkanoidComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(resources.getColor(R.color.background))
                ) {
                    Arkanoid()
                }
            }
        }
    }

    @Composable
    fun Arkanoid() {
        val navController = rememberNavController()
        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
        }

        NavHost(
            navController = navController,
            startDestination = "menu"
        ) {
            composable("menu") {
                MenuScreen(
                    onStartGameClicked = { navController.navigate("levelSelection") }
                )
            }
            composable("levelSelection") {
                LevelSelection(
                    onLevelSelected = { level ->
                        val intent = Intent(context, ArkanoidActivity::class.java)
                        intent.putExtra("level", level - 1)
                        launcher.launch(intent)
                    }
                )
            }
        }
    }

    @Composable
    fun MenuScreen(onStartGameClicked: () -> Unit) {
        val context = LocalContext.current

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Arkanoid",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onStartGameClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue))
            {
                Text(text = "Start game",color = Color.White)
            }
        }
    }

    @Composable
    fun LevelSelection(onLevelSelected: (Int) -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Level",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                LevelButton(level = 1, onClick = onLevelSelected)
                Spacer(modifier = Modifier.width(16.dp))
                LevelButton(level = 2, onClick = onLevelSelected)
                Spacer(modifier = Modifier.width(16.dp))
                LevelButton(level = 3, onClick = onLevelSelected)
            }
        }
    }

    @Composable
    fun LevelButton(level: Int, onClick: (Int) -> Unit) {
        Button(
            onClick = { onClick(level) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "Level $level")
        }
    }
}
