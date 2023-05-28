package com.example.mobilneprojekt.minesweeper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilneprojekt.R


class ChooseMinesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var text by remember { mutableStateOf(TextFieldValue("")) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.green))
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(100.dp)
                ) {
                    Text(
                        text = "Choose the number of mines!",
                        fontSize = 24.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.padding(40.dp))

                    TextField(
                        value = text,
                        textStyle = TextStyle.Default.copy(fontSize = 16.sp),
                        onValueChange = { newText ->
                            text = newText
                        },
                        modifier = Modifier
                            .width(160.dp)
                            .height(60.dp)
                    )

                    Spacer(modifier = Modifier.padding(40.dp))

                    Button(
                        onClick = { goToGame(text.text.toInt()) },
                        modifier = Modifier
                            .width(160.dp)
                            .height(80.dp)
                    ) {
                        Text(text = "Play!")
                    }
                }
            }
        }
    }

    private fun goToGame(mines: Int) {
        val intent = Intent(this, MinesweeperActivity::class.java)
        intent.putExtra("mines", mines)
        startActivity(intent)
    }
}
