package com.example.mobilneprojekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilneprojekt.ui.theme.MobilneProjektTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobilneProjektTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(40.dp)) {

                            MakeTitle(
                                title = "App title",
                                modifier = Modifier
                            )

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                item {
                                    MakeGameRow(title = "Game 1")
                                    MakeGameRow(title = "Game 2")
                                    MakeGameRow(title = "Game 3")
                                }
                            }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun MakeTitle(title: String, modifier: Modifier) {
    Text(
        text = title,
        textAlign = TextAlign.Center,
        fontSize = 60.sp
    )
}

@Composable
fun MakeGameRow(title: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Image(
            painter = painterResource(id = R.drawable.game_icon_temp),
            contentDescription = "Temp icon - change it when you deploy a game",
            Modifier.size(50.dp)
        )

        Text(
            text = title,
            textAlign = TextAlign.Center,
            fontSize = 40.sp
        )

        Button(onClick = { /*TODO*/ }) {
            Text(text = "Play!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MobilneProjektTheme {
        Greeting("Android")
    }
}