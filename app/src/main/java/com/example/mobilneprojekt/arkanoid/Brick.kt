package com.example.arkanoid

import android.graphics.RectF


class Brick(row: Int, column: Int, width: Int, height: Int, durability: Int) {
    val rect: RectF
    var durability: Int
        private set
    var visibility = true
        private set

    init {
        val padding = 1
        this.durability = durability
        rect = RectF((column * width + padding).toFloat(), (row * height + padding).toFloat(), (column * width + width - padding).toFloat(), (row * height + height - padding).toFloat())
    }

    fun setInvisible() {
        visibility = false
    }

    fun decreaseDurability() {
        durability--
    }
}