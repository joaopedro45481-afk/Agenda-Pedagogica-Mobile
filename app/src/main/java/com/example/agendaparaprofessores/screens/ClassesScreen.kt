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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.SchoolClass
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(navController: NavHostController, vm: AppViewModel = viewModel()) {
    val turmas by vm.classes.collectAsState()
    val relatorios by vm.reports.collectAsState()
    val planos by vm.lessonPlans.collectAsState()
    var editando by remember { mutableStateOf<SchoolClass?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var excluir by remember { mutableStateOf<SchoolClass?>(null) }
    var opcoesDe by remember { mutableStateOf<SchoolClass?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar("Turmas", "${turmas.size} cadastradas", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editando = null; mostrarDialogo = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nova") }
            )
        }
    ) { pad ->
        if (turmas.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    Icons.Default.Groups, "Nenhuma turma ainda",
                    "Cadastre as turmas em que você dá aula."
                )
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(turmas, key = { it.id }) { t ->
                    Card(
                        Modifier.fillMaxWidth().clickable { opcoesDe = t },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            InitialAvatar(t.name, RoxoMedio)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    listOfNotNull(
                                        t.grade?.takeIf { it.isNotBlank() },
                                        "${relatorios.count { it.classId == t.id }} relatórios",
                                        "${planos.count { it.classId == t.id }} planos"
                                    ).joinToString(" • "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { editando = t; mostrarDialogo = true }) {
                                Icon(Icons.Default.Edit, "Editar turma",
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { excluir = t }) {
                                Icon(Icons.Default.Delete, "Excluir",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // ===== Opções da turma (bottom sheet) =====
    opcoesDe?.let { t ->
        ModalBottomSheet(
            onDismissRequest = { opcoesDe = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(bottom = 28.dp)) {
                Row(
                    Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InitialAvatar(t.name, RoxoMedio)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(t.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${relatorios.count { it.classId == t.id }} relatórios",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                ListItem(
                    headlineContent = { Text("Preparar aula") },
                    supportingContent = { Text("Planeje a próxima aula de ${t.name}") },
                    leadingContent = { IconeOpcao(Icons.Default.Edit) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("plano_novo_turma/${t.id}")
                    }
                )

                ListItem(
                    headlineContent = { Text("Planos de aula") },
                    supportingContent = {
                        Text("${planos.count { it.classId == t.id }} planejada(s)")
                    },
                    leadingContent = { IconeOpcao(Icons.Default.List) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("planos_turma/${t.id}")
                    }
                )

                ListItem(
                    headlineContent = { Text("Criar relatório") },
                    supportingContent = { Text("Nova aula para ${t.name}") },
                    leadingContent = { IconeOpcao(Icons.Default.Add) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("report_form_turma/${t.id}")
                    }
                )

                ListItem(
                    headlineContent = { Text("Adicionar alunos") },
                    supportingContent = { Text("Digite 1 por 1 ou importe a lista") },
                    leadingContent = { IconeOpcao(Icons.Default.Person) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("alunos/${t.id}")
                    }
                )

                // ============ NOVO ============
                ListItem(
                    headlineContent = { Text("Frequência") },
                    supportingContent = { Text("Fazer a chamada de ${t.name}") },
                    leadingContent = { IconeOpcao(Icons.Default.HowToReg) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("frequencia/${t.id}")
                    }
                )

                ListItem(
                    headlineContent = { Text("Avaliações") },
                    supportingContent = { Text("Lançar notas de ${t.name}") },
                    leadingContent = { IconeOpcao(Icons.Default.Assessment) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("avaliacao_turma/${t.id}")
                    }
                )

                ListItem(
                    headlineContent = { Text("Desempenho") },
                    supportingContent = { Text("Ver evolução de ${t.name}") },
                    leadingContent = { IconeOpcao(Icons.Default.ShowChart) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("desempenho/${t.id}")
                    }
                )

                ListItem(
                    headlineContent = { Text("Ver relatórios") },
                    supportingContent = { Text("Só as aulas de ${t.name}") },
                    leadingContent = { IconeOpcao(Icons.Default.Description) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.clickable {
                        opcoesDe = null
                        navController.navigate("reports_turma/${t.id}")
                    }
                )
            }
        }
    }

    if (mostrarDialogo) {
        var nome by remember { mutableStateOf(editando?.name.orEmpty()) }
        var serie by remember { mutableStateOf(editando?.grade.orEmpty()) }
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text(if (editando == null) "Nova turma" else "Editar turma") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nome, onValueChange = { nome = it },
                        label = { Text("Nome da turma") },
                        placeholder = { Text("Ex: 9º Ano A") },
                        singleLine = true, shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = serie, onValueChange = { serie = it },
                        label = { Text("Descrição (opcional)") },
                        placeholder = { Text("Ex: Bagunceira") },
                        singleLine = true, shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = nome.isNotBlank(),
                    onClick = {
                        val s = serie.trim().ifBlank { null }
                        if (editando == null) vm.addClass(nome.trim(), s)
                        else vm.updateClass(editando!!.copy(name = nome.trim(), grade = s))
                        mostrarDialogo = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    excluir?.let { t ->
        ConfirmDeleteDialog(
            titulo = "Excluir turma?",
            mensagem = "Os relatórios, alunos e notas de \"${t.name}\" também serão apagados.",
            onConfirm = { vm.deleteClass(t); excluir = null },
            onDismiss = { excluir = null }
        )
    }
}

@Composable
private fun IconeOpcao(icone: ImageVector) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icone, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
