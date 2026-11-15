package com.example.tarefas.model

/* epresenta a entidade de dados da aplicação.
 * Não possui nenhuma referência a Views ou ViewModels.*/

data class Task(
    val id: Int,
    var title: String,
    var priority: String,
    var category: String = "Nenhuma",
    var isDone: Boolean = false
)
