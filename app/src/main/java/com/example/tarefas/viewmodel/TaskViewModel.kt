package com.example.tarefas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.tarefas.model.Task
import com.example.tarefas.model.TaskRepository

/*faz a ponte entre a View e o Model. Não conhece nada de Android UI (não referencia
 * Views, Contexts de tela, etc.), apenas expõe dados observáveis e ações.*/

class TaskViewModel : ViewModel() {

    val tasks: LiveData<MutableList<Task>> = TaskRepository.tasks

    fun addTask(title: String, priority: String): Task {
        return TaskRepository.addTask(title, priority)
    }

    fun toggleDone(taskId: Int) {
        TaskRepository.toggleDone(taskId)
    }

    fun updateCategory(taskId: Int, category: String) {
        TaskRepository.updateCategory(taskId, category)
    }

    fun getTask(taskId: Int): Task? = TaskRepository.findById(taskId)

    fun completedCount(): Int = tasks.value?.count { it.isDone } ?: 0

    fun totalCount(): Int = tasks.value?.size ?: 0
}
