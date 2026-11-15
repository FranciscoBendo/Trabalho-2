package com.example.tarefas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tarefas.ui.StatisticsScreen
import com.example.tarefas.ui.theme.TarefasTheme

/* Activity "casca" que hospeda a tela construída em Jetpack Compose
 * Recebe os dados via Intent enviados pela MainActivity.*/

class StatisticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val total = intent.getIntExtra(MainActivity.EXTRA_TOTAL, 0)
        val done = intent.getIntExtra(MainActivity.EXTRA_DONE, 0)

        setContent {
            TarefasTheme {
                StatisticsScreen(
                    total = total,
                    done = done,
                    onBack = { finish() }
                )
            }
        }
    }
}
