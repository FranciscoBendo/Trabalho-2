package com.example.tarefas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tarefas.adapter.CategoryAdapter
import com.example.tarefas.databinding.ActivityTaskDetailBinding
import com.example.tarefas.viewmodel.TaskViewModel

/**
 * Tela 2: detalhe da tarefa, recebida via Intent a
 * partir da MainActivity. Layout raiz é um ConstraintLayout e usa GridView
 * para escolha de categoria.*/

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var viewModel: TaskViewModel
    private var taskId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Uso de View Binding (requisito)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        // Recebendo os dados enviados via Intent
        taskId = intent.getIntExtra(MainActivity.EXTRA_TASK_ID, -1)
        val title = intent.getStringExtra(MainActivity.EXTRA_TASK_TITLE) ?: ""
        val priority = intent.getStringExtra(MainActivity.EXTRA_TASK_PRIORITY) ?: ""

        // O valor recebido é utilizado na tela de destino
        binding.tvDetailTitle.text = title
        binding.tvDetailPriority.text = "Prioridade: $priority"

        val categorias = listOf("Trabalho", "Pessoal", "Estudo", "Saúde", "Lazer", "Urgente")
        binding.gridCategories.adapter = CategoryAdapter(this, categorias)

        binding.gridCategories.setOnItemClickListener { _, _, position, _ ->
            val categoriaEscolhida = categorias[position]
            viewModel.updateCategory(taskId, categoriaEscolhida)
            binding.tvDetailCategory.text = "Categoria: $categoriaEscolhida"
        }

        binding.btnToggleDone.setOnClickListener {
            viewModel.toggleDone(taskId)
            val tarefaAtual = viewModel.getTask(taskId)
            binding.tvDetailStatus.text =
                if (tarefaAtual?.isDone == true) "Status: Concluída" else "Status: Pendente"
        }
    }
}
