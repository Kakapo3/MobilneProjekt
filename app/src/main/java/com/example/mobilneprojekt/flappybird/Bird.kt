package com.example.mobilneprojekt.flappybird

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.R

class Bird : BaseObject(){
    private var arrBms = ArrayList<Bitmap>()
    private var count = 0
    private val vFlap = 5
    private var idCurrentBitmap = 0
    var drop = 0f

    fun draw(canvas: Canvas) {
        drop()
        canvas.drawBitmap(this.bm!!, x, y, null)
    }

    private fun drop() {
        drop += 0.6.toFloat()
        y += drop
    }

    fun getArrBms(): ArrayList<Bitmap> {
        return arrBms
    }

    fun setArrBms(arrBms: ArrayList<Bitmap>) {
        this.arrBms = arrBms
        for (i in arrBms.indices) {
            this.arrBms[i] = Bitmap.createScaledBitmap(this.arrBms[i], width, height, true)
        }
    }

    override var bm: Bitmap?
        get() {
            count++
            if (count == vFlap) {
                for (i in arrBms.indices) {
                    if (i == arrBms.size - 1) {
                        idCurrentBitmap = 0
                        break
                    } else if (idCurrentBitmap == i) {
                        idCurrentBitmap = i + 1
                        break
                    }
                }
                count = 0
            }
            if (drop < 0) {
                val matrix = Matrix()
                matrix.postRotate(-25f)
                return Bitmap.createBitmap(
                    arrBms[idCurrentBitmap], 0, 0, arrBms[idCurrentBitmap].width,
                    arrBms[idCurrentBitmap].height, matrix, true
                )
            } else if (drop >= 0) {
                val matrix = Matrix()
                if (drop < 70) {
                    matrix.postRotate(-25 + drop * 2)
                } else {
                    matrix.postRotate(45f)
                }
                return Bitmap.createBitmap(
                    arrBms[idCurrentBitmap], 0, 0, arrBms[idCurrentBitmap].width,
                    arrBms[idCurrentBitmap].height, matrix, true
                )
            }
            return arrBms[idCurrentBitmap]
        }
        set(bm) {
            super.bm = bm
        }
}