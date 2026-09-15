package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*

@Composable
fun HomeScreen(navController: NavHostController, vm: AppViewModel = viewModel()) {
    val relatorios by vm.reports.collectAsState()
    val materias by vm.subjects.collectAsState()
    val turmas by vm.classes.collectAsState()
    val avaliacoes by vm.assessments.collectAsState()

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        GradientHeader {
            Text("Minha Agenda", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("Seus relatórios de aula, organizados",
                style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill("${relatorios.size}", "relatórios")
                StatPill("${avaliacoes.size}", "avaliações")
                StatPill("${materias.size}", "matérias")
                StatPill("${turmas.size}", "turmas")
            }
        }

        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Button(
                onClick = { navController.navigate("report_form") },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(10.dp))
                Text("Novo relatório de aula", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(4.dp))
            Text("Atalhos", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            HomeCard(Icons.Default.MenuBook, "Matérias", "${materias.size} cadastradas", Roxo) {
                navController.navigate("subjects")
            }
            HomeCard(Icons.Default.Groups, "Turmas", "${turmas.size} cadastradas", RoxoMedio) {
                navController.navigate("classes")
            }
            HomeCard(Icons.Default.Assignment, "Relatórios", "${relatorios.size} no total", Color(0xFFB4579C)) {
                navController.navigate("reports")
            }
            HomeCard(Icons.Default.Assessment, "Avaliações", "${avaliacoes.size} lançadas", Color(0xFF4B1FA8)) {
                navController.navigate("avaliacoes")
            }
            // ============ NOVO ============
            HomeCard(Icons.Default.ShowChart, "Desempenho", "Veja a evolução das turmas", Color(0xFF7E57C2)) {
                navController.navigate("desempenho")
            }

            if (relatorios.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Últimos relatórios", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { navController.navigate("reports") }) { Text("Ver todos") }
                }
                relatorios.take(3).forEach { r ->
                    ReportCard(
                        report = r,
                        nomeMateria = materias.firstOrNull { it.id == r.subjectId }?.name ?: "—",
                        nomeTurma = turmas.firstOrNull { it.id == r.classId }?.name ?: "—",
                        onClick = { navController.navigate("report_form/${r.id}") }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
