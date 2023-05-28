package com.example.mobilneprojekt.arkanoid

data class Level(val name: String, val bricks: List<Triple<Int, Int, Int>>)