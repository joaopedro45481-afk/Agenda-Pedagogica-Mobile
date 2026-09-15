package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.SchoolClass
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*

@Composable
fun ClassesScreen(navController: NavHostController, vm: AppViewModel = viewModel()) {
    val turmas by vm.classes.collectAsState()
    val relatorios by vm.reports.collectAsState()
    var editando by remember { mutableStateOf<SchoolClass?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var excluir by remember { mutableStateOf<SchoolClass?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("Turmas", "${turmas.size} cadastradas", onBack = { navController.popBackStack() }) },
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
                EmptyState(Icons.Default.Groups, "Nenhuma turma ainda",
                    "Cadastre as turmas em que você dá aula.")
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(turmas, key = { it.id }) { t ->
                    Card(
                        Modifier.fillMaxWidth().clickable { editando = t; mostrarDialogo = true },
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
                                    listOfNotNull(t.grade?.takeIf { it.isNotBlank() },
                                        "${relatorios.count { it.classId == t.id }} relatórios").joinToString(" • "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { excluir = t }) {
                                Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
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
                        label = { Text("Série (opcional)") },
                        placeholder = { Text("Ex: 9º ano") },
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
            dismissButton = { TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") } }
        )
    }

    excluir?.let { t ->
        ConfirmDeleteDialog(
            titulo = "Excluir turma?",
            mensagem = "Os relatórios de \"${t.name}\" também serão apagados.",
            onConfirm = { vm.deleteClass(t); excluir = null },
            onDismiss = { excluir = null }
        )
    }
}
