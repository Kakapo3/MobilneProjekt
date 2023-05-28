package com.example.mobilneprojekt.flappybird

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.R
open class BaseObject {
    var x = 0f
    var y = 0f
    var width = 0
    var height = 0
    open var bm: Bitmap? = null

    var rect: Rect?
        get() = Rect(this.x.toInt(), this.y.toInt(), (this.x+this.width).toInt(),
            (this.y+this.height).toInt()
        )
        set(rect){
            this.rect=rect
        }

    constructor() {}
    constructor(x: Float, y: Float, width: Int, height: Int) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

}