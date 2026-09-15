package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*

@Composable
fun ReportsScreen(
    navController: NavHostController,
    turmaInicial: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val relatorios by vm.reports.collectAsState()
    val materias by vm.subjects.collectAsState()
    val turmas by vm.classes.collectAsState()
    var bimestre by remember { mutableStateOf(0) }
    var turmaFiltro by remember { mutableStateOf(turmaInicial) }

    LaunchedEffect(turmas, turmaFiltro) {
        if (turmaFiltro != -1L && turmas.none { it.id == turmaFiltro }) turmaFiltro = -1L
    }

    val lista = relatorios.filter { r ->
        (turmaFiltro == -1L || r.classId == turmaFiltro) &&
                (bimestre == 0 || r.bimester == bimestre)
    }

    val nomeTurma = turmas.firstOrNull { it.id == turmaFiltro }?.name

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                if (nomeTurma != null) "Relatórios • $nomeTurma" else "Relatórios",
                "${lista.size} encontrados",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (turmaFiltro != -1L) navController.navigate("report_form_turma/$turmaFiltro")
                    else navController.navigate("report_form")
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Novo") }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            if (turmas.isNotEmpty()) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip("Todas as turmas", turmaFiltro == -1L) { turmaFiltro = -1L }
                    turmas.forEach { t ->
                        SelectableChip(t.name, turmaFiltro == t.id) { turmaFiltro = t.id }
                    }
                }
            }

            Row(
                Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableChip("Todos", bimestre == 0) { bimestre = 0 }
                (1..4).forEach { i ->
                    SelectableChip("${i}º Bim", bimestre == i) { bimestre = i }
                }
            }

            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        Icons.Default.Assignment,
                        "Nada por aqui",
                        when {
                            nomeTurma != null && bimestre != 0 ->
                                "Nenhum relatório de $nomeTurma no ${bimestre}º bimestre."
                            nomeTurma != null ->
                                "Nenhum relatório de $nomeTurma ainda. Crie o primeiro!"
                            bimestre != 0 ->
                                "Nenhum relatório no ${bimestre}º bimestre."
                            else ->
                                "Nenhum relatório ainda. Toque em \"Novo\" pra começar."
                        }
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.id }) { r ->
                        ReportCard(
                            report = r,
                            nomeMateria = materias.firstOrNull { it.id == r.subjectId }?.name ?: "—",
                            nomeTurma = turmas.firstOrNull { it.id == r.classId }?.name ?: "—",
                            onClick = { navController.navigate("report_form/${r.id}") }
                        )
                    }
                }
            }
        }
    }
}
