package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*

@Composable
fun AssessmentsScreen(
    navController: NavHostController,
    turmaInicial: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val avaliacoes by vm.assessments.collectAsState()
    val turmas by vm.classes.collectAsState()
    var bimestre by remember { mutableStateOf(0) }
    var turmaFiltro by remember { mutableStateOf(turmaInicial) }

    LaunchedEffect(turmas, turmaFiltro) {
        if (turmaFiltro != -1L && turmas.none { it.id == turmaFiltro }) turmaFiltro = -1L
    }

    val lista = avaliacoes.filter { a ->
        (turmaFiltro == -1L || a.classId == turmaFiltro) &&
                (bimestre == 0 || a.bimester == bimestre)
    }

    val nomeTurma = turmas.firstOrNull { it.id == turmaFiltro }?.name

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                if (nomeTurma != null) "Avaliações • $nomeTurma" else "Avaliações",
                "${lista.size} encontradas",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (turmaFiltro != -1L) navController.navigate("avaliacao_turma/$turmaFiltro")
                    else navController.navigate("avaliacao_nova")
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nova") }
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
                        Icons.Default.Assessment,
                        "Nenhuma avaliação",
                        "Crie uma avaliação, escolha a turma e lance as notas dos alunos."
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.id }) { a ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("avaliacao/${a.id}") },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(Modifier.height(IntrinsicSize.Min)) {
                                Box(
                                    Modifier.width(6.dp).fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                )
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        a.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        turmas.firstOrNull { it.id == a.classId }?.name ?: "—",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CalendarMonth, null,
                                            Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "%02d/%02d/%04d".format(a.day, a.month, a.year),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(14.dp))
                                        Text(
                                            "${a.bimester}º Bimestre",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
