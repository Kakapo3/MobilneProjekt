package com.example.mobilneprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilneprojekt.snake.SnakeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var gamesList = ArrayList<String>()
        gamesList.add("Game 1")
        gamesList.add("Game 2")
        gamesList.add("Game 3")

        val recycler = findViewById<RecyclerView>(R.id.eventsList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = CustomAdapter(gamesList)
        startActivity(Intent(this, SnakeActivity::class.java))
    }
}