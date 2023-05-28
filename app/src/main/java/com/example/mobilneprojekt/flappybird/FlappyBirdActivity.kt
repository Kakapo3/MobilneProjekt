package com.example.mobilneprojekt.flappybird

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.R

class FlappyBirdActivity : AppCompatActivity() {
    private var gv: GameView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dm = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(dm)
        Constants.SCREEN_WIDTH = dm.widthPixels
        Constants.SCREEN_HEIGHT = dm.heightPixels
        setContentView(R.layout.activity_flappybird)
        txt_score = findViewById(R.id.txt_score)
        txt_best_score = findViewById(R.id.txt_best_score)
        txt_score_over = findViewById(R.id.txt_score_over)
        rl_game_over = findViewById(R.id.rl_game_over)
        button_start = findViewById(R.id.button_start)
        gv = findViewById(R.id.gv)
        button_start!!.setOnClickListener(View.OnClickListener {
            gv!!.isStart=true
            txt_score!!.setVisibility(View.VISIBLE)
            button_start!!.setVisibility(View.INVISIBLE)
        })
        rl_game_over!!.setOnClickListener(View.OnClickListener {
            button_start!!.setVisibility(View.VISIBLE)
            rl_game_over!!.setVisibility(View.INVISIBLE)
            gv!!.isStart=false
            gv!!.reset()
        })
    }

    companion object {
        var txt_score: TextView? = null
        var txt_best_score: TextView? = null
        var txt_score_over: TextView? = null
        var rl_game_over: RelativeLayout? = null
        private var button_start: Button? = null
    }
}