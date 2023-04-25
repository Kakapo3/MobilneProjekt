package com.example.mobilneprojekt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val data: List<String>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var textView : TextView = view.findViewById(R.id.gameName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_selection, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position]
    }

    override fun getItemCount(): Int {
        return data.size
    }
}