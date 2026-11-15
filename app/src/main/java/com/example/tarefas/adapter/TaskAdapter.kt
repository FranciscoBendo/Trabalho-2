package com.example.tarefas.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.tarefas.R
import com.example.tarefas.model.Task

/* Adapter do ListView. Usa findViewById internamente
 * para popular cada item (item_task.xml,
 * que é construído com ConstraintLayout).*/

class TaskAdapter(
    private val context: Context,
    private var tasks: MutableList<Task>,
    private val onToggle: (Task) -> Unit
) : BaseAdapter() {

    fun updateTasks(newTasks: MutableList<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun getCount(): Int = tasks.size

    override fun getItem(position: Int): Task = tasks[position]

    override fun getItemId(position: Int): Long = tasks[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_task, parent, false)

        val task = tasks[position]

        val titleView = view.findViewById<TextView>(R.id.tvItemTitle)
        val priorityView = view.findViewById<TextView>(R.id.tvItemPriority)
        val checkBox = view.findViewById<CheckBox>(R.id.cbItemDone)

        titleView.text = task.title
        priorityView.text = "Prioridade: ${task.priority}"

        checkBox.setOnCheckedChangeListener(null)
        checkBox.isChecked = task.isDone
        checkBox.setOnCheckedChangeListener { _, _ -> onToggle(task) }

        return view
    }
}
