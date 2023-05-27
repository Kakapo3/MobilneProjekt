package com.example.mobilneprojekt.sudoku.viewModel

import androidx.lifecycle.ViewModel
import com.example.mobilneprojekt.sudoku.game.SudokuGame


class PlaySudokuViewModel : ViewModel() {
    val sudokuGame = SudokuGame()
}