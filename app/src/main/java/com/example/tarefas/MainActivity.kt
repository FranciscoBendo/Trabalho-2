package com.example.tarefas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tarefas.adapter.TaskAdapter
import com.example.tarefas.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

/*Tela inicial, lista as tarefas e permite criar novas.
 *Usa LinearLayout como layout raiz (activity_main.xml).*/

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    private lateinit var etTitle: EditText
    private lateinit var etPriority: EditText
    private lateinit var btnAdd: Button
    private lateinit var lvTasks: ListView
    private lateinit var btnStats: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Uso de findViewById (requisito)
        etTitle = findViewById(R.id.etTaskTitle)
        etPriority = findViewById(R.id.etTaskPriority)
        btnAdd = findViewById(R.id.btnAddTask)
        lvTasks = findViewById(R.id.lvTasks)
        btnStats = findViewById(R.id.btnStats)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        adapter = TaskAdapter(this, emptyList()) { task -> viewModel.toggleDone(task) }
        lvTasks.adapter = adapter

        viewModel.tasks.observe(this) { lista -> adapter.updateTasks(lista) }

        btnAdd.setOnClickListener {
            val title = etTitle.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Informe o título da tarefa", Toast.LENGTH_SHORT).show()
            } else {
                // chamada da função que recebe parâmetros, processa e retorna valor
                val novaTarefa = viewModel.addTask(title, etPriority.text.toString())
                etTitle.text.clear()
                etPriority.text.clear()
                Toast.makeText(this, "Tarefa '${novaTarefa.title}' criada", Toast.LENGTH_SHORT).show()
            }
        }

        // Navegação para TaskDetailActivity com passagem de parâmetro via Intent
        lvTasks.setOnItemClickListener { _, _, position, _ ->
            val task = adapter.getItem(position)
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra(EXTRA_TASK_ID, task.id)
            startActivity(intent)
        }

        // Navegação para a tela em Jetpack Compose, também com dados via Intent
        btnStats.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            intent.putExtra(EXTRA_TOTAL, viewModel.totalCount())
            intent.putExtra(EXTRA_DONE, viewModel.completedCount())
            startActivity(intent)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TOTAL = "extra_total"
        const val EXTRA_DONE = "extra_done"
    }
}