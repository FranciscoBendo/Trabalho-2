package com.example.tarefas.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tarefas.model.Task

/*Interface DAO*/
@Dao
interface TaskDao {
    @Insert
    suspend fun inserir(task : Task) : Long

    @Update
    suspend fun atualizar(task: Task)

    @Delete
    suspend fun deletar(task: Task)

    @Query("SELECT * FROM tarefas ORDER BY id")
    fun listar(): LiveData<List<Task>>

    @Query("SELECT * FROM tarefas WHERE id = :id")
    fun buscar(id: Int): LiveData<Task?>

}