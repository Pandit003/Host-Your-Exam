package com.example.testmaster.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testmaster.R

data class DisplayItem(
    val title: String,
    val desc: String = "",
    val iconRes: Int? = null,
    val iconText: String? = null
)

class GenericHorizontalAdapter(
    private val items: List<DisplayItem>,
    private val layoutRes: Int,
    private val onBind: (View, DisplayItem) -> Unit
) : RecyclerView.Adapter<GenericHorizontalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBind(holder.itemView, items[position])
    }

    override fun getItemCount() = items.size
}