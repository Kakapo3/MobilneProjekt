package com.example.mobilneprojekt.flappybird

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GameView(private val context: Context, attrs: AttributeSet?) : View(
    context, attrs
) {
    private var bird: Bird? = null
    private val handler: Handler
    private val r: Runnable
    private var arrPipes = ArrayList<Pipe>()
    private var sumpipe = 0
    private var distance = 0
    private var score: Int
    private var bestScore = 0
    var isStart: Boolean

    init {
        val sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE)
        if (sp != null) {
            bestScore = sp.getInt("bestscore", 0)
        }
        score = 0
        isStart = false
        initBird()
        initPipe()
        handler = Handler()
        r = Runnable { invalidate() }
    }

    private fun initPipe() {
        sumpipe = 6
        distance = 300 * Constants.SCREEN_HEIGHT / 1920
        arrPipes = ArrayList()
        for (i in 0 until sumpipe) {
            if (i < sumpipe / 2) {
                arrPipes.add(
                    Pipe(
                        (Constants.SCREEN_WIDTH + i *
                                ((Constants.SCREEN_WIDTH + 200 * Constants.SCREEN_WIDTH / 1080) / (sumpipe / 2))).toFloat(),
                        0f,
                        200 * Constants.SCREEN_WIDTH / 1080,
                        Constants.SCREEN_HEIGHT / 2
                    )
                )
                arrPipes[arrPipes.size - 1].bm=BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.pipe2
                )
                arrPipes[arrPipes.size - 1].randomY()
            } else {
                arrPipes.add(
                    Pipe(
                        arrPipes[i - sumpipe / 2].x,
                        arrPipes[i - sumpipe / 2].y
                                + arrPipes[i - sumpipe / 2].height + distance,
                        200 * Constants.SCREEN_WIDTH / 1080,
                        Constants.SCREEN_HEIGHT/2
                    )
                )
                arrPipes[arrPipes.size - 1].bm=BitmapFactory.decodeResource(
                    this.resources,
                    R.drawable.pipe1
                )
            }
        }
    }
    private fun initBird() {
        bird = Bird()
        bird!!.width=100 * Constants.SCREEN_WIDTH / 1080
        bird!!.height = 100 * Constants.SCREEN_HEIGHT / 1920
        bird!!.x=(100 * Constants.SCREEN_WIDTH / 1080).toFloat()
        bird!!.y=(Constants.SCREEN_WIDTH / 2 - bird!!.height / 2).toFloat()
        val arrBms = ArrayList<Bitmap>()
        arrBms.add(BitmapFactory.decodeResource(this.resources, R.drawable.bird1))
        arrBms.add(BitmapFactory.decodeResource(this.resources, R.drawable.bird2))
        bird!!.setArrBms(arrBms)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (isStart) {
            bird!!.draw(canvas)
            for (i in 0 until sumpipe) {
                if (bird!!.rect!!.intersect(arrPipes[i].rect!!) || bird!!.y - bird!!.height < 0 || bird!!.y > Constants.SCREEN_HEIGHT
                ) {
                    Pipe.Companion.speed = 0
                    FlappyBirdActivity.Companion.txt_score_over!!.setText(FlappyBirdActivity.Companion.txt_score!!.getText())
                    FlappyBirdActivity.Companion.txt_best_score!!.setText("best: $bestScore")
                    FlappyBirdActivity.Companion.txt_score!!.setVisibility(INVISIBLE)
                    FlappyBirdActivity.Companion.rl_game_over!!.setVisibility(VISIBLE)
                    if(score>=5){
                        achievementCompleted("fb1")
                    }
                    if(score>=10){
                        achievementCompleted("fb2")
                    }
                    if(score>=15){
                        achievementCompleted("fb3")
                    }
                }
                if (bird!!.x + bird!!.width > arrPipes[i].x + arrPipes[i].width / 2 && bird!!.x + bird!!.width <= arrPipes[i].x + arrPipes[i].width / 2 + Pipe.Companion.speed && i < sumpipe / 2) {
                    score++
                    if (score > bestScore) {
                        bestScore = score
                        val sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE)
                        val editor = sp.edit()
                        editor.putInt("bestscore", bestScore)
                        editor.apply()
                    }
                    FlappyBirdActivity.Companion.txt_score!!.setText("" + score)
                }
                if (arrPipes[i].x < -arrPipes[i].width) {
                    arrPipes[i].x=Constants.SCREEN_WIDTH.toFloat()
                    if (i < sumpipe / 2) {
                        arrPipes[i].randomY()
                    } else {
                        arrPipes[i].y = arrPipes[i - sumpipe / 2].y + arrPipes[i - sumpipe / 2].height + distance

                    }
                }
                arrPipes[i].draw(canvas)
            }
        } else {
            if (bird!!.y > Constants.SCREEN_HEIGHT / 2) {
                bird!!.drop=(-15 * Constants.SCREEN_HEIGHT / 1920).toFloat()
            }
            bird!!.draw(canvas)
        }
        handler.postDelayed(r, 10)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            bird!!.drop=-15f
        }
        return true
    }

    fun reset() {
        FlappyBirdActivity.Companion.txt_score!!.setText("0")
        score = 0
        initBird()
        initPipe()
    }

    val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")

    fun achievementCompleted(name:String){
        val currUser = Firebase.auth.currentUser?.uid
        db.getReference("accounts/${currUser}/achievements/${name}").setValue(true)
        Log.d("Dziala?","dziala")
    }
}