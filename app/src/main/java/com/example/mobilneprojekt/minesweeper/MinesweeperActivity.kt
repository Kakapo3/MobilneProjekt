package com.example.mobilneprojekt.minesweeper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.example.minesweeper.Field
import com.example.mobilneprojekt.MainActivity
import com.example.mobilneprojekt.R
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class MinesweeperActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minesweeper)

        restart()
    }

    private fun restart() {
        val fields = createBoard(9, 9)
        val mines = maxOf(minOf(intent.getIntExtra("mines", 10), 50), 5)
        addBombs(fields, mines)
        addFunctionality(fields)
        setupFlagButton()
        updateRemainingBombs(fields)
    }

    @SuppressLint("ResourceAsColor")
    private fun createBoard(cols: Int, rows: Int): MutableList<Field> {
        val board = this.findViewById<TableLayout>(R.id.Board)
        board.removeAllViews()
        val fields = mutableListOf<Field>()
        board.isShrinkAllColumns = true
        for (row in 1..rows) {
            val tr = TableRow(this)
            for (col in 1..cols) {
                val field = Field(this)
                field.id = (row.toString() + col.toString()).toInt()
                field.textSize = 15F
                //field.text = field.id.toString()
                tr.addView(field)
                fields.add(field)
            }
            board.addView(tr)
        }
        return fields
    }

    @SuppressLint("ResourceAsColor")
    private fun addFunctionality(fields : MutableList<Field>) {
        for (field in fields) {
            var toRestart = false
            field.setOnClickListener {
                if(getFlagButton().text == "Click") {
                    if(field.text != "F") {
                        if(field.hasBomb) {
                            field.text = "X"
                            Toast.makeText(this, "Boom!", Toast.LENGTH_SHORT).show()
                            restart()
                            toRestart = true
                        } else {
                            field.setTextColor(Color.BLACK)
                            field.text = getNeighboringBombs(field).toString()
                            if(getNeighboringBombs(field) == 0) {
                                field.text = ""
                                field.blank = true
                                field.setBackgroundColor(0)
                                revealNeighbours(field)
                            }
                        }
                    }
                } else {
                    if(field.text == "F") {
                        field.text = ""
                    } else if(field.text == "" && !field.blank) {
                        field.setTextColor(Color.RED)
                        field.text = "F"
                    }
                }

                if(won(fields)) {
                    Toast.makeText(this, "BRAWO!", Toast.LENGTH_SHORT).show()
                    restart()
                    toRestart = true
                }

                if(!toRestart) {
                    updateRemainingBombs(fields)
                }
            }
        }
    }

    private fun addBombs(fields : MutableList<Field>, num : Int) {
        var bombsLeft = num
        val total = fields.size
        while (bombsLeft > 0) {
            val rand = Random.nextInt(total)
            if (!fields[rand].hasBomb) {
                fields[rand].hasBomb = true
                bombsLeft -= 1
            }
        }
    }

    private fun getField(id : Int) : Field {
        return findViewById(id)
    }

    private fun getNeighbours(field: Field) : MutableList<Field> {
        val id = field.id
        val row = field.id / 10
        val col = field.id % 10
        val neighbours = mutableListOf<Field>()

        if(row > 1) {
            neighbours.add(getField(id - 10))
            if(col > 1) {
                neighbours.add(getField(id - 1))
                neighbours.add(getField(id - 11))
            }
            if(col < 9) {
                neighbours.add(getField(id + 1))
                neighbours.add(getField(id - 9))
            }
        }
        if(row < 9) {
            neighbours.add(getField(id + 10))
            if(col > 1) {
                neighbours.add(getField(id + 9))
                if (row == 1) {
                    neighbours.add(getField(id - 1))
                }
            }
            if(col < 9) {
                neighbours.add(getField(id + 11))
                if (row == 1) {
                    neighbours.add(getField(id + 1))
                }
            }
        }
        return neighbours
    }

    private fun getNeighboringBombs(field: Field) : Int {
        val neighbours = getNeighbours(field)
        var bombs = 0

        for (n in neighbours) {
            bombs += n.hasBomb()
        }

        return bombs
    }

    private fun revealNeighbours(field: Field) {
        val neighbours = getNeighbours(field)

        for (n in neighbours) {
            if(!n.blank) {
                n.performClick()
            }
        }
    }

    private fun setupFlagButton() {
        var flagButton = findViewById<Button>(R.id.flagButton)
        flagButton.text = "Click"
        flagButton.setOnClickListener {
            if(flagButton.text == "Click") {
                flagButton.text = "Flag"
            } else {
                flagButton.text = "Click"
            }
        }
    }

    @SuppressLint("WrongViewCast")
    private fun getFlagButton() : Button {
        return findViewById(R.id.flagButton)
    }

    private fun getRemainingBombs(fields: MutableList<Field>) : Int {

        var bombs = 0

        for (field in fields) {
            if(field.hasBomb) {
                bombs += 1
            }

            if(field.text == "F") {
                bombs -= 1
            }
        }
        return bombs
    }

    @SuppressLint("WrongViewCast", "SetTextI18n")
    private fun updateRemainingBombs(fields: MutableList<Field>) {
        val remBut = findViewById<TextView>(R.id.remainingBombsText)
        remBut.text = "Miny:\n" + getRemainingBombs(fields).toString()
    }

    private fun won(fields: MutableList<Field>) : Boolean {
        for (field in fields) {
            if (field.hasBomb && field.text != "F") {
                return false
            }

            if (!field.hasBomb && field.text == "F") {
                return false
            }

            if (field.text == "" && !field.blank) {
                return false
            }
        }

        return true
    }

    fun backToMenu(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}