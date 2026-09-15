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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.agendaparaprofessores.ui.screens.AssessmentFormScreen
import com.example.agendaparaprofessores.ui.screens.AssessmentsScreen
import com.example.agendaparaprofessores.ui.screens.ClassesScreen
import com.example.agendaparaprofessores.ui.screens.HomeScreen
import com.example.agendaparaprofessores.ui.screens.LessonPlanFormScreen
import com.example.agendaparaprofessores.ui.screens.LessonPlansScreen
import com.example.agendaparaprofessores.ui.screens.PerformanceScreen
import com.example.agendaparaprofessores.ui.screens.ReportFormScreen
import com.example.agendaparaprofessores.ui.screens.ReportsScreen
import com.example.agendaparaprofessores.ui.screens.StudentPerformanceScreen
import com.example.agendaparaprofessores.ui.screens.StudentsScreen
import com.example.agendaparaprofessores.ui.screens.SubjectsScreen
import com.example.agendaparaprofessores.ui.theme.AgendaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        composable("home") { HomeScreen(navController) }
                        composable("subjects") { SubjectsScreen(navController) }
                        composable("classes") { ClassesScreen(navController) }

                        // ---- Relatórios ----
                        composable("reports") { ReportsScreen(navController) }
                        composable(
                            route = "reports_turma/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            ReportsScreen(
                                navController,
                                turmaInicial = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }

                        composable("report_form") { ReportFormScreen(navController) }
                        composable(
                            route = "report_form_turma/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            ReportFormScreen(
                                navController,
                                initialClassId = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }
                        composable("report_form/{id}") { entry ->
                            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            ReportFormScreen(navController, id)
                        }

                        // ---- Relatório gerado a partir de um plano (NOVO) ----
                        composable(
                            route = "relatorio_do_plano/{planId}",
                            arguments = listOf(navArgument("planId") { type = NavType.LongType })
                        ) { entry ->
                            ReportFormScreen(
                                navController,
                                lessonPlanId = entry.arguments?.getLong("planId") ?: 0L
                            )
                        }

                        // ---- Alunos ----
                        composable(
                            route = "alunos/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            StudentsScreen(
                                navController,
                                entry.arguments?.getLong("classId") ?: -1L
                            )
                        }

                        // ---- Avaliações ----
                        composable("avaliacoes") { AssessmentsScreen(navController) }
                        composable(
                            route = "avaliacoes_turma/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            AssessmentsScreen(
                                navController,
                                turmaInicial = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }
                        composable("avaliacao_nova") { AssessmentFormScreen(navController) }
                        composable(
                            route = "avaliacao_turma/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            AssessmentFormScreen(
                                navController,
                                initialClassId = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }
                        composable(
                            route = "avaliacao/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { entry ->
                            AssessmentFormScreen(
                                navController,
                                assessmentId = entry.arguments?.getLong("id") ?: 0L
                            )
                        }

                        // ---- Desempenho (turma) ----
                        composable("desempenho") { PerformanceScreen(navController) }
                        composable(
                            route = "desempenho/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            PerformanceScreen(
                                navController,
                                turmaInicial = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }

                        // ---- Desempenho por aluno ----
                        composable("desempenho_alunos") {
                            StudentPerformanceScreen(navController)
                        }
                        composable(
                            route = "desempenho_alunos/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            StudentPerformanceScreen(
                                navController,
                                turmaInicial = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }

                        // ---- Preparador de Aula (NOVO) ----
                        composable("planos") { LessonPlansScreen(navController) }
                        composable(
                            route = "planos_turma/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            LessonPlansScreen(
                                navController,
                                turmaInicial = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }
                        composable("plano_novo") { LessonPlanFormScreen(navController) }
                        composable(
                            route = "plano_novo_turma/{classId}",
                            arguments = listOf(navArgument("classId") { type = NavType.LongType })
                        ) { entry ->
                            LessonPlanFormScreen(
                                navController,
                                initialClassId = entry.arguments?.getLong("classId") ?: -1L
                            )
                        }
                        composable(
                            route = "plano/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { entry ->
                            LessonPlanFormScreen(
                                navController,
                                planId = entry.arguments?.getLong("id") ?: 0L
                            )
                        }
                    }
                }
            }
        }
    }
}
