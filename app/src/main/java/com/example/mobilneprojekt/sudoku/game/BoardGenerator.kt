package com.example.mobilneprojekt.sudoku.game

import java.io.File
import kotlin.random.Random

fun BoardGenerator(size: Int, clues: Int): List<Cell> {
    val board = MutableList(size * size) { 0 }
    for (i in 0 until clues) {
        var index: Int
        var value: Int
        do {
            index = Random.nextInt(size * size)
            value = Random.nextInt(size) + 1
        } while (!isValid(board, index, value, size))
        board[index] = value
    }
    return getBoard(board)
}

fun isValid(board: List<Int>, index: Int, value: Int, size: Int): Boolean {
    val row = index / size
    val column = index % size
    for (i in 0 until size) {
        if (board[row * size + i] == value) return false
        if (board[i * size + column] == value) return false
    }
    val boxSize = Math.sqrt(size.toDouble()).toInt()
    val boxRowStart = row - row % boxSize
    val boxColumnStart = column - column % boxSize
    for (i in boxRowStart until boxRowStart + boxSize) {
        for (j in boxColumnStart until boxColumnStart + boxSize) {
            if (board[i * size + j] == value) return false
        }
    }
    return true
}



fun getBoard(board: List<Int>): List<Cell> {
    return List(9 * 9) { i -> Cell(i / 9, i % 9, board[i], board[i] != 0, ) }
}

fun getRandomBoard(size: Int) :List<Cell> {
    val board = MutableList(size * size) {0}

    val fileName = if (size == 9) "app/java/com.example.sudoku/game/board_9x9.txt" else "app/java/com.example.sudoku/game/16x16.txt"
    val boardId = if (size == 9) Random.nextInt(0, 50) else /*Random.nextInt(0,1)*/ 0
    val boardLines = if (size == 9) 10 else 17

    File(fileName).useLines { lines ->
        lines.drop(boardId * 10).take(boardLines - 1).forEach {line ->
            for(char in line) {
                board.add(char.toInt())
            }
        }
    }
    return getBoard(board)
}