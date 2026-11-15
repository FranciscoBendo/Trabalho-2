package com.example.tarefas.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.tarefas.R

/* Adapter do GridView. Cada célula do grid representa
 * uma categoria que pode ser aplicada à tarefa.*/

class CategoryAdapter(
    private val context: Context,
    private val categories: List<String>
) : BaseAdapter() {

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): String = categories[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_category, parent, false)

        val label = view.findViewById<TextView>(R.id.tvCategoryLabel)
        label.text = categories[position]

        return view
    }
}
