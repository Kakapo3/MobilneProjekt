package com.example.arkanoid

data class Level(val name: String, val bricks: List<Triple<Int, Int, Int>>)