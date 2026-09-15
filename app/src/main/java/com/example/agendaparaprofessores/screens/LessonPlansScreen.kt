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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.LessonPlan
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*

private val OPCOES_STATUS = listOf(
    "todas" to "Todas",
    "planejada" to "Planejadas",
    "ministrada" to "Ministradas",
    "adiada" to "Adiadas"
)

@Composable
private fun corDoStatus(status: String): Color = when (status.lowercase()) {
    "ministrada" -> VerdeTexto
    "adiada" -> VermelhoTexto
    else -> Roxo
}

private fun dataDoPlano(p: LessonPlan): String {
    val d = p.plannedDay
    val m = p.plannedMonth
    val a = p.plannedYear
    return if (d != null && m != null && a != null) "%02d/%02d/%04d".format(d, m, a)
    else "sem data"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPlansScreen(
    navController: NavHostController,
    turmaInicial: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val planos by vm.lessonPlans.collectAsState()
    val turmas by vm.classes.collectAsState()
    val materias by vm.subjects.collectAsState()

    var turmaFiltro by remember(turmaInicial) { mutableStateOf(turmaInicial) }
    var statusFiltro by remember { mutableStateOf("todas") }
    var opcoesDe by remember { mutableStateOf<LessonPlan?>(null) }
    var excluir by remember { mutableStateOf<LessonPlan?>(null) }

    val lista = planos
        .filter { turmaFiltro == -1L || it.classId == turmaFiltro }
        .filter { statusFiltro == "todas" || it.status.equals(statusFiltro, true) }
        .sortedWith(
            compareBy(
                { it.plannedYear ?: 9999 },
                { it.plannedMonth ?: 12 },
                { it.plannedDay ?: 31 }
            )
        )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = "Preparador de aulas",
                subtitulo = "${lista.size} plano(s)",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(
                        if (turmaFiltro > 0L) "plano_novo_turma/$turmaFiltro" else "plano_novo"
                    )
                },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nova aula") }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OPCOES_STATUS.forEach { (valor, rotulo) ->
                    SelectableChip(rotulo, statusFiltro == valor) { statusFiltro = valor }
                }
            }

            if (turmas.size > 1) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip("Todas as turmas", turmaFiltro == -1L) { turmaFiltro = -1L }
                    turmas.forEach { t ->
                        SelectableChip(t.name, turmaFiltro == t.id) { turmaFiltro = t.id }
                    }
                }
            }

            if (lista.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        Icons.Default.Edit, "Nenhum plano por aqui",
                        "Toque em \"Nova aula\" para planejar a próxima aula."
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.id }) { p ->
                        CartaoPlano(
                            plano = p,
                            nomeTurma = turmas.firstOrNull { it.id == p.classId }?.name ?: "—",
                            nomeMateria = materias.firstOrNull { it.id == p.subjectId }?.name ?: "—",
                            onClick = { opcoesDe = p }
                        )
                    }
                }
            }
        }
    }

    // ===== Detalhes e ações do plano =====
    opcoesDe?.let { p ->
        ModalBottomSheet(
            onDismissRequest = { opcoesDe = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DetalhePlano(
                plano = p,
                nomeTurma = turmas.firstOrNull { it.id == p.classId }?.name ?: "—",
                nomeMateria = materias.firstOrNull { it.id == p.subjectId }?.name ?: "—",
                onEditar = { opcoesDe = null; navController.navigate("plano/${p.id}") },
                onDuplicar = { vm.duplicarPlano(p); opcoesDe = null },
                onGerarRelatorio = { opcoesDe = null; navController.navigate("relatorio_do_plano/${p.id}") },
                onMinistrada = { vm.atualizarStatusPlano(p.id, "ministrada"); opcoesDe = null },
                onAdiada = { vm.atualizarStatusPlano(p.id, "adiada"); opcoesDe = null },
                onExcluir = { opcoesDe = null; excluir = p }
            )
        }
    }

    excluir?.let { p ->
        ConfirmDeleteDialog(
            titulo = "Excluir plano?",
            mensagem = "\"${p.title}\" será apagado. Os relatórios já criados não são afetados.",
            onConfirm = { vm.deleteLessonPlan(p); excluir = null },
            onDismiss = { excluir = null }
        )
    }
}

@Composable
private fun CartaoPlano(
    plano: LessonPlan,
    nomeTurma: String,
    nomeMateria: String,
    onClick: () -> Unit
) {
    val cor = corDoStatus(plano.status)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(cor.copy(alpha = 0.7f)))
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plano.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .background(cor.copy(alpha = 0.14f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            plano.status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = cor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "$nomeTurma • $nomeMateria",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (plano.objective.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        plano.objective,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(dataDoPlano(plano), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    plano.durationMinutes?.let { min ->
                        Spacer(Modifier.width(14.dp))
                        Icon(Icons.Default.Schedule, null, Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(6.dp))
                        Text("${min} min", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalhePlano(
    plano: LessonPlan,
    nomeTurma: String,
    nomeMateria: String,
    onEditar: () -> Unit,
    onDuplicar: () -> Unit,
    onGerarRelatorio: () -> Unit,
    onMinistrada: () -> Unit,
    onAdiada: () -> Unit,
    onExcluir: () -> Unit
) {
    val cor = corDoStatus(plano.status)

    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 28.dp)
    ) {
        Text(plano.title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "$nomeTurma • $nomeMateria • ${dataDoPlano(plano)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .background(cor.copy(alpha = 0.14f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                plano.status.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = cor,
                fontWeight = FontWeight.Bold
            )
        }

        BlocoDetalhe("Objetivo", plano.objective)
        BlocoDetalhe("Conteúdo", plano.content)
        BlocoDetalhe("Como será a aula", plano.methodology)
        BlocoDetalhe("Materiais", plano.materials)
        BlocoDetalhe("Atividades", plano.activities)
        BlocoDetalhe("Tarefa de casa", plano.homework)
        BlocoDetalhe("Observações", plano.notes)

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))

        ItemAcao("Gerar relatório desta aula", "Registrar o que realmente aconteceu",
            Icons.Default.Description, onGerarRelatorio)
        ItemAcao("Editar plano", "Alterar o planejamento", Icons.Default.Edit, onEditar)
        ItemAcao("Duplicar plano", "Reusar em outra turma", Icons.Default.ContentCopy, onDuplicar)
        ItemAcao("Marcar como ministrada", "A aula já aconteceu", Icons.Default.Check, onMinistrada)
        ItemAcao("Marcar como adiada", "A aula não aconteceu", Icons.Default.Schedule, onAdiada)
        ItemAcao("Excluir plano", "Apagar o planejamento", Icons.Default.Delete, onExcluir)
    }
}

@Composable
private fun BlocoDetalhe(titulo: String, texto: String) {
    if (texto.isBlank()) return
    Spacer(Modifier.height(16.dp))
    Text(
        titulo,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(4.dp))
    Text(texto, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun ItemAcao(
    titulo: String,
    subtitulo: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = { Text(subtitulo) },
        leadingContent = { Icon(icone, null, tint = MaterialTheme.colorScheme.primary) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
