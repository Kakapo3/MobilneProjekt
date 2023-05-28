package com.example.mobilneprojekt.sudoku.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.R
import com.example.mobilneprojekt.sudoku.game.Cell
import com.example.mobilneprojekt.sudoku.view.custom.SudokuBoardView
import com.example.mobilneprojekt.sudoku.viewModel.PlaySudokuViewModel


class PlaySudokuActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener,
    EndGameDialogFragment.EndGameDialogListener {

    private lateinit var viewModel: PlaySudokuViewModel
    private lateinit var sudokuBoardView : SudokuBoardView
    private lateinit var numberButtons: List<Button>
    private lateinit var notesButton : ImageButton
    private lateinit var deleteButton : ImageButton

    private lateinit var gameDifficulty : String

    private lateinit var endGameDialogFragment: EndGameDialogFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_sudoku)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        gameDifficulty = intent.getStringExtra("Difficulty").toString()

        sudokuBoardView = findViewById(R.id.sudokuBoardView)
        sudokuBoardView.registerListener(this)

        viewModel = ViewModelProvider(this)[PlaySudokuViewModel()::class.java]



        viewModel.sudokuGame.selectedCellLiveData.observe(this, Observer { updateSelectedCellUI(it) })
        viewModel.sudokuGame.cellsLiveData.observe(this, Observer { updateCells(it) })
        viewModel.sudokuGame.isTakingNotesLiveData.observe(this, Observer { updateNoteTakingUI(it) })
        viewModel.sudokuGame.highlitedKeysLiveData.observe(this, Observer { updateHighlightedKeys(it) })
        viewModel.sudokuGame.gameFinishedLiveData.observe(this, Observer { gameEnd(it) })

        numberButtons = listOf<Button>(findViewById(R.id.oneButton), findViewById(R.id.twoButton), findViewById(R.id.threeButton),
            findViewById(R.id.fourButton), findViewById(R.id.fiveButton), findViewById(R.id.sixButton),
            findViewById(R.id.sevenButton), findViewById(R.id.eightButton), findViewById(R.id.nineButton))

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
            }
        }

        notesButton = findViewById(R.id.notesButton)
        notesButton.setOnClickListener { viewModel.sudokuGame.changeNoteTakingState() }

        deleteButton = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener { viewModel.sudokuGame.delete()}

    }

    private fun gameEnd(it: Boolean) {
        endGameDialogFragment = EndGameDialogFragment().apply {
            arguments = Bundle().apply {
                putString("gameOutcome", if (it) "Win!!!" else "Lose")
            }
            listener = this@PlaySudokuActivity
        }
        endGameDialogFragment.show(supportFragmentManager, "endGameDialog")

    }

    private fun updateHighlightedKeys(set: Set<Int>?) = set?.let {
        numberButtons.forEachIndexed {index, button ->
            val color = if (set.contains(index + 1)) ContextCompat.getColor(this, R.color.teal_700) else Color.CYAN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                button.background.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.MULTIPLY))
            }
        }
    }

    private fun updateNoteTakingUI(isNoteTaking: Boolean?) = isNoteTaking?.let {
        val color = if (it) ContextCompat.getColor(this, R.color.teal_700) else R.color.mediumGreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            notesButton.background.setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.MULTIPLY))
        }
    }

    private fun updateCells(cells: List<Cell>?) = cells?.let {
        sudokuBoardView.updateCells(cells)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        sudokuBoardView.updateSelectedCellUI(cell.first, cell.second)
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.sudokuGame.updateSelectedCell(row, col)
    }

    override fun onResetGame() {
        supportFragmentManager
            .beginTransaction()
            .remove(endGameDialogFragment)
            .commit()
        val intent = Intent(this, PlaySudokuActivity::class.java)
        intent.putExtra("difficultyLevel", gameDifficulty)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onChangeDif() {
        supportFragmentManager
            .beginTransaction()
            .remove(endGameDialogFragment)
            .commit()

        val intent = Intent(this, DifficultyActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onCloseGame() {
        supportFragmentManager
            .beginTransaction()
            .remove(endGameDialogFragment)
            .commit()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}