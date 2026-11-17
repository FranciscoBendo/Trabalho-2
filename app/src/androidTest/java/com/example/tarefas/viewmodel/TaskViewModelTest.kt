package com.example.tarefas.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tarefas.awaitValue
import com.example.tarefas.data.AppDataBase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes do TaskViewModel como ele está: ele usa o banco REAL do app (singleton).
 * Por isso as tabelas são limpas antes e depois de cada teste. ATENÇÃO: isso apaga
 * as tarefas do app instalado no aparelho/emulador onde os testes rodam.
 */
@RunWith(AndroidJUnit4::class)
class TaskViewModelTest {

    private lateinit var app: Application
    private lateinit var viewModel: TaskViewModel

    @Before
    fun preparar() {
        app = ApplicationProvider.getApplicationContext()
        AppDataBase.getInstance(app).clearAllTables()
        viewModel = TaskViewModel(app)
    }

    @After
    fun limpar() {
        AppDataBase.getInstance(app).clearAllTables()
    }

    @Test
    fun addTask_removeEspacosDoTitulo_ePersiste() {
        val retornada = viewModel.addTask("  Estudar Kotlin  ", "Alta")

        assertEquals("Estudar Kotlin", retornada.title)
        assertEquals("Alta", retornada.priority)

        val lista = viewModel.tasks.awaitValue { it.isNotEmpty() }
        assertEquals("Estudar Kotlin", lista.single().title)
    }

    @Test
    fun addTask_prioridadeEmBranco_usaNormal() {
        val retornada = viewModel.addTask("Lavar o carro", "   ")

        assertEquals("Normal", retornada.priority)

        val lista = viewModel.tasks.awaitValue { it.isNotEmpty() }
        assertEquals("Normal", lista.single().priority)
    }

    @Test
    fun totalCount_refleteQuantidadeDeTarefas() {
        viewModel.addTask("A", "Alta")
        viewModel.addTask("B", "Média")
        viewModel.addTask("C", "Baixa")

        viewModel.tasks.awaitValue { it.size == 3 }

        assertEquals(3, viewModel.totalCount())
    }

    @Test
    fun toggleDone_marcaTarefaEAtualizaCompletedCount() {
        viewModel.addTask("A", "Alta")
        viewModel.addTask("B", "Baixa")
        val lista = viewModel.tasks.awaitValue { it.size == 2 }
        val alvo = lista.first()

        viewModel.toggleDone(alvo)

        val atualizada = viewModel.tasks.awaitValue { l -> l.any { it.isDone } }
        assertTrue(atualizada.single { it.id == alvo.id }.isDone)
        assertEquals(1, viewModel.completedCount())
        assertEquals(2, viewModel.totalCount())
    }

    @Test
    fun updateCategory_alteraApenasACategoria() {
        viewModel.addTask("Ler livro", "Média")
        val original = viewModel.tasks.awaitValue { it.isNotEmpty() }.single()

        viewModel.updateCategory(original, "Estudo")

        val atualizada = viewModel.tasks
            .awaitValue { l -> l.any { it.category == "Estudo" } }
            .single()
        assertEquals("Estudo", atualizada.category)
        assertEquals("Ler livro", atualizada.title)
        assertEquals("Média", atualizada.priority)
        assertFalse(atualizada.isDone)
    }
}
