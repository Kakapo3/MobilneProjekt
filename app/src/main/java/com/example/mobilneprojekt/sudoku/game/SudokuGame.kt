package com.example.mobilneprojekt.sudoku.game

import androidx.lifecycle.MutableLiveData
import kotlin.math.sqrt

class SudokuGame() {

    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()
    val isTakingNotesLiveData = MutableLiveData<Boolean>()
    val highlitedKeysLiveData = MutableLiveData<Set<Int>>()
    val gameFinishedLiveData = MutableLiveData<Boolean>()


    private var selectedRow = -1
    private var selectedCol = -1
    private var isTakingNotes = false
    private var boardSize = 9
    private lateinit var board: Board
    private lateinit var difficulty : String

    init {
        boardSize = 9
        difficulty = "Medium"
        var clues : Int
        if (difficulty == "Easy" )
            clues = 200
        else if (difficulty == "Medium" )
            clues = 150
        else if (difficulty == "Hard" )
            clues = 100
        else {
            clues = 20
            //boardSize = 16
        }
        val cells = BoardGenerator(boardSize, clues)
        //val cells = getRandomBoard(boardSize)
        board = Board(boardSize, cells)

        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.cells)

        isTakingNotesLiveData.postValue(isTakingNotes)

    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow, selectedCol)
        if (cell.isStartingCell) return

        if(isTakingNotes) {
            if (cell.notes.contains(number))
                cell.notes.remove(number)
            else
                cell.notes.add(number)
            highlitedKeysLiveData.postValue(cell.notes)
        } else {
            cell.value = number
        }
        cellsLiveData.postValue(board.cells)

        if(isBoardCompleted()) {
            gameFinishedLiveData.postValue(isValidSudoku())
        }
    }

    fun updateSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        if (!cell.isStartingCell) {
            selectedCol = col
            selectedRow = row
            selectedCellLiveData.postValue(Pair(row, col))

            if (isTakingNotes) {
                highlitedKeysLiveData.postValue(cell.notes)
            }
        }
    }

    fun changeNoteTakingState() {
        isTakingNotes = !isTakingNotes
        isTakingNotesLiveData.postValue(isTakingNotes)

        val curNotes = if (isTakingNotes) {
            board.getCell(selectedRow, selectedCol).notes
        } else {
            setOf<Int>()
        }
        highlitedKeysLiveData.postValue(curNotes)
    }

    fun delete() {
        val cell = board.getCell(selectedRow, selectedCol)
        if (isTakingNotes) {
            cell.notes.clear()
            highlitedKeysLiveData.postValue(setOf())
        } else {
            cell.value = 0
        }
        cellsLiveData.postValue(board.cells)
    }

    fun isBoardCompleted(): Boolean {
        board.cells.forEach {if (it.value == 0) return false }
        return true
    }

    fun isValidSudoku(): Boolean {
        val intBoard = List(boardSize * boardSize) {i -> board.cells[i].value}
        val N = sqrt(intBoard.size.toDouble()).toInt()
        val root_N = sqrt(N.toDouble()).toInt()

        // sprawdzenie unikalności elementów w rzędach i kolumnach
        for (i in 0 until N) {
            val row = intBoard.slice(i * N until (i + 1) * N)
            val column = (0 until N).map { intBoard[it * N + i] }
            if (row.distinct().size != N || column.distinct().size != N) {
                return false
            }
        }

        // sprawdzenie unikalności elementów w kwadratach
        for (i in 0 until N step root_N) {
            for (j in 0 until N step root_N) {
                val square = (0 until root_N).flatMap { x ->
                    (0 until root_N).map { y -> intBoard[(i + x) * N + (j + y)] }
                }
                if (square.distinct().size != N) {
                    return false
                }
            }
        }

        return true
    }
}