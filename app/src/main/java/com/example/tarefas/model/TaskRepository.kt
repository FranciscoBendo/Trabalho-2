package com.example.tarefas.model

import androidx.lifecycle.MutableLiveData

/*Repositório responsável por manter e manipular a lista de tarefas em memória.
 * É um singleton (object) para que os dados persistam entre as diferentes Activities da aplicação.*/
object TaskRepository {

    private var nextId = 1
    private val taskList = mutableListOf<Task>()

    val tasks = MutableLiveData<MutableList<Task>>(taskList)

    /*Função Kotlin que RECEBE PARÂMETROS, REALIZA PROCESSAMENTO
     * (cria a tarefa, gera o id, adiciona na lista) e RETORNA UM VALOR (a tarefa criada).*/

    fun addTask(title: String, priority: String): Task {
        val novaTarefa = Task(id = nextId, title = title, priority = priority)
        nextId += 1
        taskList.add(novaTarefa)
        tasks.value = taskList
        return novaTarefa
    }

    fun toggleDone(taskId: Int) {
        taskList.find { it.id == taskId }?.let { it.isDone = !it.isDone }
        tasks.value = taskList
    }

    fun updateCategory(taskId: Int, category: String) {
        taskList.find { it.id == taskId }?.let { it.category = category }
        tasks.value = taskList
    }

    fun findById(taskId: Int): Task? = taskList.find { it.id == taskId }

    /*Outra função que recebe parâmetro (a lista de tarefas), processa
     *(calcula o percentual de conclusão) e retorna um valor (Int).*/

    fun calculateCompletionPercentage(list: List<Task>): Int {
        if (list.isEmpty()) return 0
        val concluidas = list.count { it.isDone }
        return ((concluidas.toDouble() / list.size.toDouble()) * 100).toInt()
    }
}
