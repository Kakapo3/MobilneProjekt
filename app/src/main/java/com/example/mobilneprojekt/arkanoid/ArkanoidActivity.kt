package com.example.mobilneprojekt.arkanoid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobilneprojekt.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.abs


class ArkanoidActivity : ComponentActivity() {
    var arkanoidView: ArkanoidView? = null
    var levelNumber: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        levelNumber = intent.getIntExtra("level", 0)
        arkanoidView = ArkanoidView(this)
        setContent {
            AndroidView(
                factory = {
                    arkanoidView!!
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    inner class ArkanoidView(context: Context?) : SurfaceView(context), Runnable {
        var gameThread: Thread? = null
        var surfaceHolder: SurfaceHolder
        @Volatile var playing = false
        var paused = true
        lateinit var canvas: Canvas
        var paint: Paint
        var fps: Long = 0
        private var timeThisFrame: Long = 0
        var screenX = 0
        var screenY = 0
        var paddle: Paddle
        var ball: Ball
        var bricks = arrayOfNulls<Brick>(169)
        var numBricks = 0
        var score = 0
        var lives = 3
        val brickColors: ArrayList<Int> = arrayListOf(
            resources.getColor(R.color.yellow_block),
            resources.getColor(R.color.light_orange_block),
            resources.getColor(R.color.orange_block),
            resources.getColor(R.color.light_red_block),
            resources.getColor(R.color.red_block)
        )

        val levels = listOf(
            Level("Level 1",
                listOf(
                    Triple(3, 0, 1), Triple(3, 1, 1), Triple(3, 2, 1), Triple(3, 3, 1), Triple(3, 4, 1), Triple(3, 5, 1), Triple(3, 6, 1), Triple(3, 7, 1), Triple(3, 8, 1), Triple(3, 9, 1), Triple(3, 10, 1), Triple(3, 11, 1), Triple(3, 12, 1),
                    Triple(4, 0, 2), Triple(4, 1, 2), Triple(4, 2, 2), Triple(4, 3, 2), Triple(4, 4, 2), Triple(4, 5, 2), Triple(4, 6, 2), Triple(4, 7, 2), Triple(4, 8, 2), Triple(4, 9, 2), Triple(4, 10, 2), Triple(4, 11, 2), Triple(4, 12, 2),
                    Triple(5, 0, 3), Triple(5, 1, 3), Triple(5, 2, 3), Triple(5, 3, 3), Triple(5, 4, 3), Triple(5, 5, 3), Triple(5, 6, 3), Triple(5, 7, 3), Triple(5, 8, 3), Triple(5, 9, 3), Triple(5, 10, 3), Triple(5, 11, 3), Triple(5, 12, 3),
                    Triple(6, 0, 4), Triple(6, 1, 4), Triple(6, 2, 4), Triple(6, 3, 4), Triple(6, 4, 4), Triple(6, 5, 4), Triple(6, 6, 4), Triple(6, 7, 4), Triple(6, 8, 4), Triple(6, 9, 4), Triple(6, 10, 4), Triple(6, 11, 4), Triple(6, 12, 4),
                    Triple(7, 0, 5), Triple(7, 1, 5), Triple(7, 2, 5), Triple(7, 3, 5), Triple(7, 4, 5), Triple(7, 5, 5), Triple(7, 6, 5), Triple(7, 7, 5), Triple(7, 8, 5), Triple(7, 9, 5), Triple(7, 10, 5), Triple(7, 11, 5), Triple(7, 12, 5),
                )
            ),
            Level("Level 2",
                listOf(
                    Triple(3, 0, 4), Triple(3, 1, 4), Triple(3, 2, 4), Triple(3, 3, 4), Triple(3, 4, 4), Triple(3, 5, 4), Triple(3, 6, 4), Triple(3, 7, 4), Triple(3, 8, 4), Triple(3, 9, 4), Triple(3, 10, 4), Triple(3, 11, 4), Triple(3, 12, 4),
                    Triple(5, 0, 2), Triple(5, 1, 2), Triple(5, 2, 2), Triple(5, 3, 2), Triple(5, 4, 2), Triple(5, 5, 2), Triple(5, 6, 2), Triple(5, 7, 2), Triple(5, 8, 2), Triple(5, 9, 2), Triple(5, 10, 2), Triple(5, 11, 2), Triple(5, 12, 2),
                    Triple(7, 0, 4), Triple(7, 1, 4), Triple(7, 2, 4), Triple(7, 3, 4), Triple(7, 4, 4), Triple(7, 5, 4), Triple(7, 6, 4), Triple(7, 7, 4), Triple(7, 8, 4), Triple(7, 9, 4), Triple(7, 10, 4), Triple(7, 11, 4), Triple(7, 12, 4),
                    Triple(9, 0, 2), Triple(9, 1, 2), Triple(9, 2, 2), Triple(9, 3, 2), Triple(9, 4, 2), Triple(9, 5, 2), Triple(9, 6, 2), Triple(9, 7, 2), Triple(9, 8, 2), Triple(9, 9, 2), Triple(9, 10, 2), Triple(9, 11, 2), Triple(9, 12, 2),
                    Triple(11, 0, 4), Triple(11, 1, 4), Triple(11, 2, 4), Triple(11, 3, 4), Triple(11, 4, 4), Triple(11, 5, 4), Triple(11, 6, 4), Triple(11, 7, 4), Triple(11, 8, 4), Triple(11, 9, 4), Triple(11, 10, 4), Triple(11, 11, 4), Triple(11, 12, 4),
                )
            ),
//            Level("Test",
//                listOf(
//                    Triple(3, 10, 1)
//                )
//            ),
            Level("Level 3",
                listOf(
                    Triple(2, 0, 1), Triple(3, 0, 1), Triple(4, 0, 1), Triple(5, 0, 1), Triple(6, 0, 1), Triple(7, 0, 1), Triple(8, 0, 1), Triple(9, 0, 1), Triple(10, 0, 1), Triple(11, 0, 1), Triple(12, 0, 1), Triple(13, 0, 1), Triple(14, 0, 5),
                    Triple(3, 1, 1), Triple(4, 1, 1), Triple(5, 1, 1), Triple(6, 1, 1), Triple(7, 1, 1), Triple(8, 1, 1), Triple(9, 1, 1), Triple(10, 1, 1), Triple(11, 1, 1), Triple(12, 1, 1), Triple(13, 1, 1), Triple(14, 1, 5),
                    Triple(4, 2, 1), Triple(5, 2, 1), Triple(6, 2, 1), Triple(7, 2, 1), Triple(8, 2, 1), Triple(9, 2, 1), Triple(10, 2, 1), Triple(11, 2, 1), Triple(12, 2, 1), Triple(13, 2, 1), Triple(14, 2, 5),
                    Triple(5, 3, 1), Triple(6, 3, 1), Triple(7, 3, 1), Triple(8, 3, 1), Triple(9, 3, 1), Triple(10, 3, 1), Triple(11, 3, 1), Triple(12, 3, 1), Triple(13, 3, 1), Triple(14, 3, 5),
                    Triple(6, 4, 1), Triple(7, 4, 1), Triple(8, 4, 1), Triple(9, 4, 1), Triple(10, 4, 1), Triple(11, 4, 1), Triple(12, 4, 1), Triple(13, 4, 1), Triple(14, 4, 5),
                    Triple(7, 5, 1), Triple(8, 5, 1), Triple(9, 5, 1), Triple(10, 5, 1), Triple(11, 5, 1), Triple(12, 5, 1), Triple(13, 5, 1), Triple(14, 5, 5),
                    Triple(8, 6, 1), Triple(9, 6, 1), Triple(10, 6, 1), Triple(11, 6, 1), Triple(12, 6, 1), Triple(13, 6, 1), Triple(14, 6, 5),
                    Triple(9, 7, 1), Triple(10, 7, 1), Triple(11, 7, 1), Triple(12, 7, 1), Triple(13, 7, 1), Triple(14, 7, 5),
                    Triple(10, 8, 1), Triple(11, 8, 1), Triple(12, 8, 1), Triple(13, 8, 1), Triple(14, 8, 5),
                    Triple(11, 9, 1), Triple(12, 9, 1), Triple(13, 9, 1), Triple(14, 9, 5),
                    Triple(12, 10, 1), Triple(13, 10, 1), Triple(14, 10, 5),
                    Triple(13, 11, 1), Triple(14, 11, 5),
                    Triple(14, 12, 1),
                )
            )
        )

        init {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenX = displayMetrics.widthPixels
            screenY = displayMetrics.heightPixels
            surfaceHolder = holder
            paint = Paint()
            paddle = Paddle(screenX, screenY)
            ball = Ball(screenX, screenY)
            createBricksAndRestart()
        }

        override fun run() {
            while (playing) {
                val startFrameTime = System.currentTimeMillis()

                if (!paused) {
                    update()
                }

                draw()

                timeThisFrame = System.currentTimeMillis() - startFrameTime

                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame
                }
            }
        }

        fun update() {
            paddle.update(fps);
            ball.update(fps);

            for (i in 0 until numBricks) {
                if (bricks[i]?.visibility == true) {
                    if (bricks[i]?.let { RectF.intersects(it.rect, ball.rect) } == true) {
                        bricks[i]!!.decreaseDurability()
                        if (bricks[i]!!.durability == 0) {
                            bricks[i]!!.setInvisible()
                            score = score + 10
                        }

                        val ballCenterX = ball.rect.centerX()
                        val ballCenterY = ball.rect.centerY()
                        val brickCenterX = bricks[i]!!.rect.centerX()
                        val brickCenterY = bricks[i]!!.rect.centerY()

                        val width = bricks[i]!!.rect.width()
                        val height = bricks[i]!!.rect.height()

                        val deltaX = (brickCenterX - ballCenterX) / (width)
                        val deltaY = (brickCenterY - ballCenterY) / (height / 2f)

                        val absDeltaX = abs(deltaX)
                        val absDeltaY = abs(deltaY)

                        if (absDeltaX > absDeltaY) {
                            if (deltaX > 0) {
                                Log.v("Collision", "left")
                                ball.reverseXVelocity()
                                ball.clearObstacleLeftX(bricks[i]!!.rect.left)
                            } else {
                                Log.v("Collision", "right")
                                ball.reverseXVelocity()
                                ball.clearObstacleRightX(bricks[i]!!.rect.right)
                            }
                        } else {
                            if (deltaY > 0) {
                                Log.v("Collision", "top")
                                ball.reverseYVelocity()
                                ball.clearObstacleTopY(bricks[i]!!.rect.top)
                            } else {
                                Log.v("Collision", "bottom")
                                ball.reverseYVelocity()
                                ball.clearObstacleBottomY(bricks[i]!!.rect.bottom)
                            }
                        }
                    }
                }
            }

            if (RectF.intersects(paddle.rectMiddle, ball.rect)) {
                ball.reverseYVelocity()
                ball.clearObstacleTopY(paddle.rectMiddle.top - 2)
            }
            else if (RectF.intersects(paddle.rectLeft, ball.rect)) {
                ball.xVelocity = -abs(ball.xVelocity)
                ball.reverseYVelocity()
                ball.clearObstacleTopY(paddle.rectLeft.top - 2)
            }
            else if (RectF.intersects(paddle.rectRight, ball.rect)) {
                ball.xVelocity = abs(ball.xVelocity)
                ball.reverseYVelocity()
                ball.clearObstacleTopY(paddle.rectRight.top - 2)
            }

            if (ball.rect.bottom > screenY) {
                ball.reverseYVelocity()
                ball.clearObstacleTopY((screenY - 2).toFloat())

                lives--

                ball.reset(screenX, screenY)
                paddle.reset(screenX, screenY)
                paused = true

                if (lives == 0) {
                    createBricksAndRestart()
                }
            }

            if (ball.rect.top < 0) {
                ball.reverseYVelocity()
                ball.clearObstacleTopY(12f)
            }

            if (ball.rect.left < 0) {
                ball.reverseXVelocity()
                ball.clearObstacleRightX(2f)
            }

            if (ball.rect.right > screenX - 10) {
                ball.reverseXVelocity()
                ball.clearObstacleRightX((screenX - 22).toFloat())
            }

            if (score == numBricks * 10) {
                paused = true
                createBricksAndRestart()
            }
        }

        fun draw() {
            if (surfaceHolder.surface.isValid) {
                canvas = surfaceHolder.lockCanvas()
                canvas.drawColor(resources.getColor(R.color.background))

                paint.color = resources.getColor(R.color.ball)
                canvas.drawRect(ball.rect, paint)

                paint.color = resources.getColor(R.color.paddle_middle)
                canvas.drawRect(paddle.rectMiddle, paint)
                paint.color = resources.getColor(R.color.paddle_sides)
                canvas.drawRect(paddle.rectLeft, paint)
                canvas.drawRect(paddle.rectRight, paint)

                for (i in 0 until numBricks) {
                    if (bricks[i]?.visibility == true) {
                        paint.color = brickColors[bricks[i]?.durability?.minus(1)!!]
                        bricks[i]?.let { canvas.drawRect(it.rect, paint) }
                    }
                }

                paint.color = Color.argb(255, 255, 255, 255)
                paint.textSize = 40f
                canvas.drawText(levels[levelNumber!!].name + "   Score: $score   Lives: $lives", 10f, 50f, paint)

                if (score == numBricks * 10) {
                    paint.textSize = 90f
                    if (levelNumber == 0){
                        achievementCompleted("arkanoid1")
                    }
                    else if (levelNumber == 1) {
                        achievementCompleted("arkanoid2")
                    }
                    else if (levelNumber == 2) {
                        achievementCompleted("arkanoid3")
                    }
                    canvas.drawText("You won!", (screenX / 4).toFloat(), (screenY / 2).toFloat(), paint)
                }

                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        fun createBricksAndRestart() {
            val level = levels[levelNumber!!]
            ball.reset(screenX, screenY)
            paddle.reset(screenX, screenY)
            val brickWidth = screenX / 13
            val brickHeight = screenY / 20

            numBricks = 0

            for (brick in level.bricks) {
                bricks[numBricks++] = Brick(brick.first, brick.second, brickWidth, brickHeight, brick.third)
            }

            if (lives === 0) {
                score = 0
                lives = 3
            }
        }

        fun pause() {
            playing = false

            try {
                gameThread!!.join()
            } catch (e: InterruptedException) {
                Log.e("Error:", "joining thread")
            }
        }

        fun resume() {
            playing = true
            gameThread = Thread(this)
            gameThread!!.start()
        }

        override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    paused = false

                    if (motionEvent.x > screenX / 2) {
                        paddle.setMovementState(paddle.RIGHT)
                    } else {
                        paddle.setMovementState(paddle.LEFT)
                    }
                }

                MotionEvent.ACTION_UP -> paddle.setMovementState(paddle.STOPPED)
            }

            return true
        }
    }

    override fun onResume() {
        super.onResume()

        arkanoidView!!.resume()
    }

    override fun onPause() {
        super.onPause()

        arkanoidView!!.pause()
    }

    val db = Firebase.database("https://projekt-mobilki-aa7ab-default-rtdb.europe-west1.firebasedatabase.app/")

    fun achievementCompleted(name: String) { val currUser = Firebase.auth.currentUser?.uid
        db.getReference("accounts/${currUser}/achievements/${name}").setValue(true)
    }
}