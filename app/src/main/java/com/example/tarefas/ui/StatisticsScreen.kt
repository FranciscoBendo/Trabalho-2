package com.example.tarefas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api

/* Função Kotlin que recebe parâmetros (total e concluídas), realiza um
 * processamento (cálculo do percentual) e retorna um valor (Int).
 * Usada pela tela em Jetpack Compose.*/

fun calcularPercentualConcluido(total: Int, concluidas: Int): Int {
    if (total <= 0) return 0
    return ((concluidas.toDouble() / total.toDouble()) * 100).toInt()
}

/* Tela 3 (Jetpack Compose): mostra as estatísticas das tarefas cadastradas.
 * Recebe os dados via Intent (extras lidos na StatisticsActivity).*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(total: Int, done: Int, onBack: () -> Unit) {
    val percentual = calcularPercentualConcluido(total, done)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Estatísticas") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Resumo das Tarefas", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Total de tarefas: $total")
            Text(text = "Tarefas concluídas: $done")
            Spacer(modifier = Modifier.height(24.dp))

            @Suppress("DEPRECATION")
            LinearProgressIndicator(
                progress = percentual / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$percentual% concluído", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack) {
                Text("Voltar")
            }
        }
    }
}
