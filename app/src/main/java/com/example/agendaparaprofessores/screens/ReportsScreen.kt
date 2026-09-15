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
fun ReportsScreen(navController: NavHostController, vm: AppViewModel = viewModel()) {
    val relatorios by vm.reports.collectAsState()
    val materias by vm.subjects.collectAsState()
    val turmas by vm.classes.collectAsState()
    var bimestre by remember { mutableStateOf(0) }   // 0 = todos
    val lista = if (bimestre == 0) relatorios else relatorios.filter { it.bimester == bimestre }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar("Relatórios", "${lista.size} encontrados", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("report_form") },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Novo") }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableChip("Todos", bimestre == 0) { bimestre = 0 }
                (1..4).forEach { i ->
                    SelectableChip("${i}º Bim", bimestre == i) { bimestre = i }
                }
            }

            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(Icons.Default.Assignment, "Nada por aqui",
                        "Nenhum relatório neste filtro. Crie um novo ou mude o bimestre.")
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
