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
import com.example.agendaparaprofessores.data.Subject
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*

@Composable
fun SubjectsScreen(navController: NavHostController, vm: AppViewModel = viewModel()) {
    val materias by vm.subjects.collectAsState()
    val relatorios by vm.reports.collectAsState()
    var editando by remember { mutableStateOf<Subject?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var excluir by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("Matérias", "${materias.size} cadastradas", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editando = null; mostrarDialogo = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nova") }
            )
        }
    ) { pad ->
        if (materias.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Default.MenuBook, "Nenhuma matéria ainda",
                    "Cadastre as matérias que você leciona para começar.")
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(materias, key = { it.id }) { m ->
                    Card(
                        Modifier.fillMaxWidth().clickable { editando = m; mostrarDialogo = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            InitialAvatar(m.name)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(m.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${relatorios.count { it.subjectId == m.id }} relatórios",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { excluir = m }) {
                                Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogo) {
        var texto by remember { mutableStateOf(editando?.name.orEmpty()) }
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text(if (editando == null) "Nova matéria" else "Editar matéria") },
            text = {
                OutlinedTextField(
                    value = texto, onValueChange = { texto = it },
                    label = { Text("Nome da matéria") },
                    placeholder = { Text("Ex: Matemática") },
                    singleLine = true, shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                Button(
                    enabled = texto.isNotBlank(),
                    onClick = {
                        if (editando == null) vm.addSubject(texto.trim())
                        else vm.updateSubject(editando!!.copy(name = texto.trim()))
                        mostrarDialogo = false
                    }
                ) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") } }
        )
    }

    excluir?.let { m ->
        ConfirmDeleteDialog(
            titulo = "Excluir matéria?",
            mensagem = "Os relatórios de \"${m.name}\" também serão apagados.",
            onConfirm = { vm.deleteSubject(m); excluir = null },
            onDismiss = { excluir = null }
        )
    }
}
