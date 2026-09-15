package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.agendaparaprofessores.data.LessonReport
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun bimestreDoMes(mes: Int) = when (mes) {
    in 2..4 -> 1
    in 5..6 -> 2
    in 7..9 -> 3
    else -> 4
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormScreen(
    navController: NavHostController,
    reportId: Long = 0L,
    initialClassId: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val materias by vm.subjects.collectAsState()
    val turmas by vm.classes.collectAsState()

    var editando by remember { mutableStateOf(reportId != 0L) }
    var materiaId by remember { mutableStateOf<Long?>(null) }
    var turmaId by remember {
        mutableStateOf(if (initialClassId > 0L) initialClassId else null)
    }
    var titulo by remember { mutableStateOf("") }
    var resumo by remember { mutableStateOf("") }
    var dificuldade by remember { mutableStateOf("Médio") }
    var data by remember { mutableStateOf(LocalDate.now()) }
    var bimestre by remember { mutableStateOf(bimestreDoMes(LocalDate.now().monthValue)) }
    var erro by remember { mutableStateOf<String?>(null) }
    var mostrarData by remember { mutableStateOf(false) }
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) {
        if (reportId != 0L) {
            vm.getReport(reportId)?.let { r ->
                editando = true
                materiaId = r.subjectId; turmaId = r.classId
                titulo = r.title; resumo = r.summary
                dificuldade = r.difficulty
                data = LocalDate.of(r.year, r.month, r.day)
                bimestre = r.bimester
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = if (editando) "Editar relatório" else "Novo relatório",
                subtitulo = "Aula de ${data.dayOfMonth}/${data.monthValue}/${data.year}",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                    Button(
                        onClick = {
                            when {
                                materiaId == null -> erro = "Escolha a matéria"
                                turmaId == null -> erro = "Escolha a turma"
                                titulo.isBlank() -> erro = "Escreva o título da aula"
                                else -> {
                                    vm.saveReport(
                                        LessonReport(
                                            id = reportId,
                                            subjectId = materiaId!!,
                                            classId = turmaId!!,
                                            title = titulo.trim(),
                                            summary = resumo.trim(),
                                            difficulty = dificuldade,
                                            day = data.dayOfMonth,
                                            month = data.monthValue,
                                            year = data.year,
                                            bimester = bimestre
                                        )
                                    )
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            if (editando) "Salvar alterações" else "Salvar relatório",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (editando) {
                        TextButton(
                            onClick = { confirmarExclusao = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Excluir relatório", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            erro?.let {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(10.dp))
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            SectionCard("Aula", Icons.Default.MenuBook) {
                DropdownField(
                    label = "Matéria",
                    itens = materias.map { it.id to it.name },
                    selecionadoId = materiaId,
                    placeholder = "Selecione a matéria"
                ) { materiaId = it; erro = null }
                Spacer(Modifier.height(12.dp))
                DropdownField(
                    label = "Turma",
                    itens = turmas.map { it.id to it.name },
                    selecionadoId = turmaId,
                    placeholder = "Selecione a turma"
                ) { turmaId = it; erro = null }
            }

            SectionCard("Detalhes", Icons.Default.Description) {
                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it; erro = null },
                    label = { Text("Título da aula") },
                    placeholder = { Text("Ex: Aula sobre Progressão Aritmética") },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = resumo, onValueChange = { resumo = it },
                    label = { Text("Resumo da aula") },
                    placeholder = { Text("Ex: aula voltada a ensinar PA para a turma") },
                    minLines = 4, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Dificuldade da turma", Icons.Default.Speed) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Fácil", "Médio", "Difícil").forEach { d ->
                        SelectableChip(d, dificuldade == d) { dificuldade = d }
                    }
                }
            }

            SectionCard("Data e bimestre", Icons.Default.CalendarMonth) {
                OutlinedButton(
                    onClick = { mostrarData = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Dia ${data.dayOfMonth} de ${data.monthValue} de ${data.year}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("Bimestre", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { i ->
                        SelectableChip("${i}º", bimestre == i) { bimestre = i }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (mostrarData) {
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = data.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarData = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { ms ->
                        val d = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        data = d
                        bimestre = bimestreDoMes(d.monthValue)
                    }
                    mostrarData = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarData = false }) { Text("Cancelar") } }
        ) { DatePicker(state = estado) }
    }

    if (confirmarExclusao) {
        ConfirmDeleteDialog(
            titulo = "Excluir relatório?",
            mensagem = "Essa ação não pode ser desfeita.",
            onConfirm = {
                vm.reports.value.firstOrNull { it.id == reportId }?.let { vm.deleteReport(it) }
                confirmarExclusao = false
                navController.popBackStack()
            },
            onDismiss = { confirmarExclusao = false }
        )
    }
}
