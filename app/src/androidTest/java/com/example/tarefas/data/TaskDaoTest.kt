package com.example.tarefas.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tarefas.getOrAwaitValue
import com.example.tarefas.model.Task
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes do TaskDao com um banco EM MEMÓRIA, separado do banco real do app.
 * Rodam no emulador/aparelho (src/androidTest).
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var db: AppDataBase
    private lateinit var dao: TaskDao

    @Before
    fun criarBanco() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java).build()
        dao = db.taskDao()
    }

    @After
    fun fecharBanco() {
        db.close()
    }

    @Test
    fun inserir_retornaIdsPositivosEDistintos() {
        runBlocking {
            val id1 = dao.inserir(Task(title = "A", priority = "Alta"))
            val id2 = dao.inserir(Task(title = "B", priority = "Baixa"))

            assertTrue(id1 > 0)
            assertTrue(id2 > id1)
        }
    }

    @Test
    fun listar_retornaTarefasOrdenadasPorId() {
        runBlocking {
            dao.inserir(Task(title = "Primeira", priority = "Alta"))
            dao.inserir(Task(title = "Segunda", priority = "Média"))
            dao.inserir(Task(title = "Terceira", priority = "Baixa"))

            val lista = dao.listar().getOrAwaitValue()

            assertEquals(listOf("Primeira", "Segunda", "Terceira"), lista.map { it.title })
        }
    }

    @Test
    fun atualizar_persisteNovosValores() {
        runBlocking {
            val id = dao.inserir(Task(title = "Estudar", priority = "Alta")).toInt()

            // Valores padrão da entidade
            val original = dao.buscar(id).getOrAwaitValue()!!
            assertEquals("Nenhuma", original.category)
            assertFalse(original.isDone)

            dao.atualizar(original.copy(category = "Estudo", isDone = true))

            val atualizada = dao.buscar(id).getOrAwaitValue()!!
            assertEquals("Estudo", atualizada.category)
            assertTrue(atualizada.isDone)
            assertEquals("Estudar", atualizada.title)
        }
    }

    @Test
    fun deletar_removeApenasATarefaIndicada() {
        runBlocking {
            val id1 = dao.inserir(Task(title = "A", priority = "Alta")).toInt()
            dao.inserir(Task(title = "B", priority = "Baixa"))

            val primeira = dao.buscar(id1).getOrAwaitValue()!!
            dao.deletar(primeira)

            val restantes = dao.listar().getOrAwaitValue()
            assertEquals(1, restantes.size)
            assertEquals("B", restantes[0].title)
            assertNull(dao.buscar(id1).getOrAwaitValue())
        }
    }

    @Test
    fun buscar_idInexistente_retornaNull() {
        assertNull(dao.buscar(9999).getOrAwaitValue())
    }
}
