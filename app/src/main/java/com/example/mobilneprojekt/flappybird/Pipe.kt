package com.example.mobilneprojekt.flappybird

import android.graphics.Bitmap
import android.graphics.Canvas
import java.util.*
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.R

class Pipe() : BaseObject() {
    constructor(x: Float, y: Float, width: Int, height: Int) : this() {
        this.x=x
        this.y=y
        this.width=width
        this.height=height
    }
    init {
        speed = 10 * Constants.SCREEN_WIDTH / 1080
    }

    fun draw(canvas: Canvas) {
        x -= speed.toFloat()
        canvas.drawBitmap(this.bm!!, x, y, null)
    }

    fun randomY() {
        val r = Random()
        y = (r.nextInt(0 + height / 4 + 1) - height / 4).toFloat()
    }

    override var bm: Bitmap?
        get() = super.bm
        set(value: Bitmap?) {
            super.bm=Bitmap.createScaledBitmap(value!!,width,height,true)
        }

    companion object {
        var speed: Int = 0
    }
}