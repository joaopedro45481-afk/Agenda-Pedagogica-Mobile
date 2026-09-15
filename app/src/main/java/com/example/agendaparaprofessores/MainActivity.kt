package com.example.agendaparaprofessores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.agendaparaprofessores.ui.screens.ClassesScreen
import com.example.agendaparaprofessores.ui.screens.HomeScreen
import com.example.agendaparaprofessores.ui.screens.ReportFormScreen
import com.example.agendaparaprofessores.ui.screens.ReportsScreen
import com.example.agendaparaprofessores.ui.screens.SubjectsScreen
import com.example.agendaparaprofessores.ui.theme.AgendaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Desenha por baixo da barra de status (o gradiente roxo fica bonito assim)
        enableEdgeToEdge()

        setContent {
            AgendaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("subjects") {
                            SubjectsScreen(navController)
                        }
                        composable("classes") {
                            ClassesScreen(navController)
                        }
                        composable("reports") {
                            ReportsScreen(navController)
                        }
                        // Novo relatório
                        composable("report_form") {
                            ReportFormScreen(navController)
                        }
                        // Editar relatório existente
                        composable("report_form/{id}") { entry ->
                            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            ReportFormScreen(navController, id)
                        }
                    }
                }
            }
        }
    }
}
