package com.example.arkanoid

import android.graphics.RectF
import java.util.*


class Ball(screenX: Int, screenY: Int) {
    var rect: RectF
    var xVelocity = 200f
    var yVelocity = -400f
    var ballWidth = 10f
    var ballHeight = 10f

    init {
        rect = RectF()
    }

    fun update(fps: Long) {
        rect.left = rect.left + xVelocity / fps
        rect.top = rect.top + yVelocity / fps
        rect.right = rect.left + ballWidth
        rect.bottom = rect.top - ballHeight
    }

    fun reverseYVelocity() {
        yVelocity = -yVelocity
    }

    fun reverseXVelocity() {
        xVelocity = -xVelocity
    }

    fun clearObstacleTopY(y: Float) {
        rect.bottom = y
        rect.top = y - ballHeight
    }

    fun clearObstacleBottomY(y: Float) {
        rect.bottom = y + ballHeight
        rect.top = y
    }

    fun clearObstacleRightX(x: Float) {
        rect.left = x
        rect.right = x + ballWidth
    }

    fun clearObstacleLeftX(x: Float) {
        rect.left = x - ballWidth
        rect.right = x
    }

    fun reset(x: Int, y: Int) {
        rect.left = (x / 2).toFloat()
        rect.top = (y * 19 / 20).toFloat()
        rect.right = x / 2 + ballWidth
        rect.bottom = y * 19 / 20 - ballHeight
    }
}