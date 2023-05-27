package com.example.mobilneprojekt.sudoku.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mobilneprojekt.R

class DifficultyActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var letterViews: List<TextView>
    private val colors = listOf(
        R.color.lightGreen,
        R.color.mediumGreen,
        R.color.darkGreen,
        R.color.darkerGreen
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.difficulty_activity)

        val buttonEasy = findViewById<Button>(R.id.buttonEasy)
        val buttonMedium = findViewById<Button>(R.id.buttonMedium)
        val buttonHard = findViewById<Button>(R.id.buttonHard)
        val buttonExpert = findViewById<Button>(R.id.buttonExpert)

        val descriptionEasy = findViewById<TextView>(R.id.descriptionEasy)
        val descriptionMedium = findViewById<TextView>(R.id.descriptionMedium)
        val descriptionHardy = findViewById<TextView>(R.id.descriptionHard)
        val descriptionExpert = findViewById<TextView>(R.id.descriptionExpert)

        buttonEasy.setOnClickListener { startMainActivity("Easy") }
        buttonMedium.setOnClickListener { startMainActivity("Medium") }
        buttonHard.setOnClickListener { startMainActivity("Hard") }
        buttonExpert.setOnClickListener { startMainActivity("Expert") }

        letterViews = listOf(
            findViewById(R.id.letterS),
            findViewById(R.id.letterU1),
            findViewById(R.id.letterD),
            findViewById(R.id.letterO),
            findViewById(R.id.letterK),
            findViewById(R.id.letterU),
            findViewById(R.id.letterG),
            findViewById(R.id.letterA),
            findViewById(R.id.letterM),
            findViewById(R.id.letterE),
        )
        for (i in letterViews.indices) {
            letterViews[i].setTextColor(ContextCompat.getColor(this, colors[i % colors.size]))
        }

        handler.postDelayed(changeColorsRunnable, 2000)
        setAnimation(buttonEasy, descriptionEasy)
        setAnimation(buttonMedium, descriptionMedium)
        setAnimation(buttonHard, descriptionHardy)
        setAnimation(buttonExpert, descriptionExpert)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setAnimation(button: Button, textView: TextView) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Animacja przesunięcia przycisku do góry
                    val upAnimation = TranslateAnimation(0f, 0f, 0f, -20f).apply {
                        duration = 500
                        fillAfter = true
                    }
                    button.startAnimation(upAnimation)

                    // Pokazanie opisu trudności
                    val fadeInAnimation = AlphaAnimation(0f, 1f).apply {
                        duration = 500
                        fillAfter = true
                    }
                    textView.visibility = View.VISIBLE
                    textView.startAnimation(fadeInAnimation)
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Animacja przesunięcia przycisku w dół
                    val downAnimation = TranslateAnimation(0f, 0f, -20f, 0f).apply {
                        duration = 500
                        fillAfter = true
                    }
                    button.startAnimation(downAnimation)

                    // Ukrycie opisu trudności
                    val fadeOutAnimation = AlphaAnimation(1f, 0f).apply {
                        duration = 500
                        fillAfter = true
                    }
                    textView.startAnimation(fadeOutAnimation)
                    textView.visibility = View.GONE
                }
            }

            v?.onTouchEvent(event) ?: true
        }
    }

    private val changeColorsRunnable = object : Runnable {
        override fun run() {
            val firstColor = letterViews[0].currentTextColor
            for (i in 0 until letterViews.size - 1) {
                letterViews[i].setTextColor(letterViews[i + 1].currentTextColor)
            }
            letterViews[letterViews.size - 1].setTextColor(firstColor)
            handler.postDelayed(this, 100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(changeColorsRunnable)
    }

    private fun startMainActivity(difficulty: String) {
        val intent = Intent(this, PlaySudokuActivity::class.java)
        intent.putExtra("Difficulty", difficulty)
        startActivity(intent)
    }

}
