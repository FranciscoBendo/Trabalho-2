package com.example.tarefas.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.tarefas.data.AppDataBase
import com.example.tarefas.model.Task
import kotlinx.coroutines.launch

/* Faz a ponte entre a View e o Model. Não referencia Views nem Contexts de tela:
 * usa apenas o Application para obter o banco. Expõe dados observáveis e ações. */

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDataBase.getInstance(application).taskDao()

    val tasks: LiveData<List<Task>> = dao.listar()

    fun getTask(taskId: Int): LiveData<Task?> = dao.buscar(taskId)

    // Recebe parâmetros, processa (limpa o texto, aplica prioridade padrão) e retorna valor
    fun addTask(title: String, priority: String): Task {
        val nova = Task(
            title = title.trim(),
            priority = priority.trim().ifEmpty { "Normal" }
        )
        viewModelScope.launch { dao.inserir(nova) }
        return nova
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch { dao.atualizar(task.copy(isDone = !task.isDone)) }
    }

    fun updateCategory(task: Task, category: String) {
        viewModelScope.launch { dao.atualizar(task.copy(category = category)) }
    }

    fun completedCount(): Int = tasks.value?.count { it.isDone } ?: 0

    fun totalCount(): Int = tasks.value?.size ?: 0
}