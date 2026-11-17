package com.example.tarefas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tarefas.adapter.CategoryAdapter
import com.example.tarefas.databinding.ActivityTaskDetailBinding
import com.example.tarefas.model.Task
import com.example.tarefas.viewmodel.TaskViewModel

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var viewModel: TaskViewModel
    private var tarefaAtual: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Uso de View Binding (requisito)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        val taskId = intent.getIntExtra(MainActivity.EXTRA_TASK_ID, -1)
        if (taskId == -1) { finish(); return }

        val categorias = listOf("Trabalho", "Pessoal", "Estudo", "Saúde", "Lazer", "Urgente")
        binding.gridCategories.adapter = CategoryAdapter(this, categorias)

        // A tela sempre reflete o que está no banco
        viewModel.getTask(taskId).observe(this) { t ->
            if (t == null) return@observe
            tarefaAtual = t
            binding.tvDetailTitle.text = t.title
            binding.tvDetailPriority.text = getString(R.string.label_priority_format, t.priority)
            binding.tvDetailCategory.text = getString(R.string.label_category_format, t.category)
            binding.tvDetailStatus.setText(if (t.isDone) R.string.status_done else R.string.status_pending)
            binding.btnToggleDone.setText(if (t.isDone) R.string.btn_mark_pending else R.string.btn_mark_done)
        }

        binding.gridCategories.setOnItemClickListener { _, _, position, _ ->
            tarefaAtual?.let { viewModel.updateCategory(it, categorias[position]) }
        }

        binding.btnToggleDone.setOnClickListener {
            tarefaAtual?.let { viewModel.toggleDone(it) }
        }
    }
}