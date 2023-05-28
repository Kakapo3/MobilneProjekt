package com.example.mobilneprojekt.sudoku.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.mobilneprojekt.R


class EndGameDialogFragment : DialogFragment() {

    private lateinit var gameOutcomeTextView: TextView
    private lateinit var buttonExit: Button
    private lateinit var buttonPlayAgain: Button
    private lateinit var buttonChangeDif: Button

    var listener: EndGameDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_end_game, container, false)

        gameOutcomeTextView = view.findViewById(R.id.gameOutcomeTextView)
        buttonExit = view.findViewById(R.id.buttonExit)
        buttonPlayAgain = view.findViewById(R.id.buttonPlayAgain)
        buttonChangeDif = view.findViewById(R.id.buttonChangeDif)

        buttonPlayAgain.setOnClickListener {
            listener?.onResetGame()
        }

        val gameOutcome = arguments?.getString("gameOutcome")
        gameOutcomeTextView.text = gameOutcome

        buttonExit.setOnClickListener {
            listener?.onCloseGame()
        }


        buttonChangeDif.setOnClickListener {
            listener?.onChangeDif()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        return dialog
    }

    interface EndGameDialogListener {
        fun onResetGame()
        fun onChangeDif()
        fun onCloseGame()
    }
}