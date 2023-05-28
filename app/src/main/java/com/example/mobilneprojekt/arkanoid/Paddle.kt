package com.example.mobilneprojekt.arkanoid

import android.graphics.RectF

class Paddle(screenX: Int, screenY: Int) {
    private val length: Float
    private val height = 20f
    private val paddleSpeed = 350f
    private var x: Float
    private val y: Float
    private val leftBorder: Float
    private val rightBorder: Float

    val STOPPED = 0
    val LEFT = 1
    val RIGHT = 2
    val rectMiddle: RectF
    val rectLeft: RectF
    val rectRight: RectF

    private var paddleMoving = STOPPED

    init {
        x = (screenX / 2).toFloat()
        y = (screenY * 19 / 20).toFloat()
        length = (screenX / 15).toFloat()
        rectLeft = RectF(x, y, x + length / 5, y + height)
        rectMiddle = RectF(x + length / 5, y, x + length * 4 / 5, y + height)
        rectRight = RectF(x + length * 4 / 5, y, x + length, y + height)
        leftBorder = 0f
        rightBorder = screenX.toFloat() - length
    }

    fun setMovementState(state: Int) {
        paddleMoving = state
    }

    fun update(fps: Long) {
        if (paddleMoving == LEFT) {
            x = leftBorder.coerceAtLeast(x - paddleSpeed / fps)
        }
        else if (paddleMoving == RIGHT) {
            x = rightBorder.coerceAtMost(x + paddleSpeed / fps)
        }

        rectLeft.left = x
        rectLeft.right = x + length / 5
        rectMiddle.left = x + length / 5
        rectMiddle.right = x + length * 4 / 5
        rectRight.left = x + length * 4 / 5
        rectRight.right = x + length
    }

    fun reset(x: Int, y: Int) {
        this.x = (x / 2).toFloat()

        rectLeft.left = (x / 2).toFloat()
        rectLeft.top = (y * 19 / 20).toFloat()
        rectLeft.right = (x / 2).toFloat() + length / 5
        rectLeft.bottom = (y * 19 / 20).toFloat() + height

        rectMiddle.left = (x / 2).toFloat() + length / 5
        rectMiddle.top = (y * 19 / 20).toFloat()
        rectMiddle.right = (x / 2).toFloat() + length * 4 / 5
        rectMiddle.bottom = (y * 19 / 20).toFloat() + height

        rectRight.left = (x / 2).toFloat() + length * 4 / 5
        rectRight.top = (y * 19 / 20).toFloat()
        rectRight.right = (x / 2).toFloat() + length
        rectRight.bottom = (y * 19 / 20).toFloat() + height
    }
}