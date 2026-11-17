package com.example.tarefas.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/* epresenta a entidade de dados da aplicação.
 * Não possui nenhuma referência a Views ou ViewModels.*/

@Entity(tableName = "tarefas")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val priority: String,
    var category: String = "Nenhuma",
    var isDone: Boolean = false
)
