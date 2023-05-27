package com.example.minesweeper

import android.content.Context
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton

class Field(context: Context) : AppCompatButton(context) {

    var hasBomb = false
    var blank = false

    fun hasBomb() : Int {
        if (hasBomb) {
            return 1
        } else {
            return 0
        }
    }
}