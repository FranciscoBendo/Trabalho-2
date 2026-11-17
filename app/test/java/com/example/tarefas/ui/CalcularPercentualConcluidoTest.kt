package com.example.tarefas.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/** Testes unitários (JVM) da função pura calcularPercentualConcluido. */
class CalcularPercentualConcluidoTest {

    @Test
    fun semTarefas_retornaZero() {
        assertEquals(0, calcularPercentualConcluido(total = 0, concluidas = 0))
    }

    @Test
    fun totalNegativo_retornaZero() {
        assertEquals(0, calcularPercentualConcluido(total = -3, concluidas = 2))
    }

    @Test
    fun nenhumaConcluida_retornaZero() {
        assertEquals(0, calcularPercentualConcluido(total = 5, concluidas = 0))
    }

    @Test
    fun todasConcluidas_retorna100() {
        assertEquals(100, calcularPercentualConcluido(total = 4, concluidas = 4))
    }

    @Test
    fun percentualFracionado_truncaParaBaixo() {
        // 2 de 3 = 66,67%; a função usa toInt(), então o resultado é 66 (não 67)
        assertEquals(66, calcularPercentualConcluido(total = 3, concluidas = 2))
    }
}
