package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.LessonPlan
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Etiquetas prontas: o rótulo é bonito, o valor salvo é minúsculo (igual ao ViewModel). */
private val STATUS_RAPIDOS = listOf(
    "Planejada" to "planejada",
    "Ministrada" to "ministrada",
    "Adiada" to "adiada"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPlanFormScreen(
    navController: NavHostController,
    planId: Long = 0L,
    initialClassId: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val materias by vm.subjects.collectAsState()
    val turmas by vm.classes.collectAsState()

    val editando = planId != 0L

    var titulo by remember { mutableStateOf("") }
    var materiaId by remember { mutableStateOf<Long?>(null) }
    var turmaId by remember {
        mutableStateOf(if (initialClassId > 0L) initialClassId else null)
    }
    var objetivo by remember { mutableStateOf("") }
    var conteudo by remember { mutableStateOf("") }
    var metodologia by remember { mutableStateOf("") }
    var materiais by remember { mutableStateOf(listOf<String>()) }
    var atividades by remember { mutableStateOf(listOf<String>()) }
    var tarefa by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }
    var duracao by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("planejada") }
    var data by remember { mutableStateOf(LocalDate.now()) }
    var erro by remember { mutableStateOf<String?>(null) }
    var mostrarData by remember { mutableStateOf(false) }
    var confirmarExclusao by remember { mutableStateOf(false) }

    LaunchedEffect(planId) {
        if (planId != 0L) {
            vm.getLessonPlan(planId)?.let { p ->
                titulo = p.title
                materiaId = p.subjectId
                turmaId = p.classId ?: turmaId
                objetivo = p.objective
                conteudo = p.content
                metodologia = p.methodology
                materiais = p.materials.lines().map { it.trim() }.filter { it.isNotEmpty() }
                atividades = p.activities.lines().map { it.trim() }.filter { it.isNotEmpty() }
                tarefa = p.homework
                duracao = p.durationMinutes?.toString().orEmpty()
                status = p.status
                observacoes = p.notes
                if (p.plannedYear != null && p.plannedMonth != null && p.plannedDay != null) {
                    data = LocalDate.of(p.plannedYear, p.plannedMonth!!, p.plannedDay!!)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = if (editando) "Editar plano" else "Preparar uma aula",
                subtitulo = "Para ${data.dayOfMonth}/${data.monthValue}/${data.year}",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                    Button(
                        onClick = {
                            when {
                                titulo.isBlank() -> erro = "Escreva o título da aula"
                                turmaId == null -> erro = "Escolha a turma"
                                else -> {
                                    vm.saveLessonPlan(
                                        LessonPlan(
                                            id = planId,
                                            subjectId = materiaId,
                                            classId = turmaId,
                                            title = titulo.trim(),
                                            objective = objetivo.trim(),
                                            content = conteudo.trim(),
                                            methodology = metodologia.trim(),
                                            materials = materiais
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                                .joinToString("\n"),
                                            activities = atividades
                                                .map { it.trim() }
                                                .filter { it.isNotEmpty() }
                                                .joinToString("\n"),
                                            homework = tarefa.trim(),
                                            durationMinutes = duracao
                                                .filter { it.isDigit() }
                                                .toIntOrNull(),
                                            plannedDay = data.dayOfMonth,
                                            plannedMonth = data.monthValue,
                                            plannedYear = data.year,
                                            status = status.trim().ifBlank { "planejada" },
                                            notes = observacoes.trim()
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
                            if (editando) "Salvar alterações" else "Salvar plano",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (editando) {
                        TextButton(
                            onClick = { confirmarExclusao = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Excluir plano", color = MaterialTheme.colorScheme.error) }
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null,
                            tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.width(10.dp))
                        Text(it, color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            SectionCard("Identificação", Icons.Default.Info) {
                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it; erro = null },
                    label = { Text("Título da aula") },
                    placeholder = { Text("Ex: Introdução à Progressão Aritmética") },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                DropdownField(
                    label = "Matéria",
                    itens = materias.map { it.id to it.name },
                    selecionadoId = materiaId,
                    placeholder = "Selecione a matéria (opcional)"
                ) { materiaId = it }
                Spacer(Modifier.height(12.dp))
                DropdownField(
                    label = "Turma",
                    itens = turmas.map { it.id to it.name },
                    selecionadoId = turmaId,
                    placeholder = "Selecione a turma"
                ) { turmaId = it; erro = null }
            }

            SectionCard("Quando e quanto tempo", Icons.Default.CalendarMonth) {
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
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = duracao, onValueChange = { duracao = it },
                    label = { Text("Duração (min)") },
                    placeholder = { Text("Ex: 50") },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Objetivo", Icons.Default.Flag) {
                OutlinedTextField(
                    value = objetivo, onValueChange = { objetivo = it },
                    label = { Text("O que os alunos devem aprender?") },
                    placeholder = { Text("Ex: identificar o termo geral de uma PA") },
                    minLines = 3, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Conteúdo", Icons.Default.MenuBook) {
                OutlinedTextField(
                    value = conteudo, onValueChange = { conteudo = it },
                    label = { Text("O que será trabalhado") },
                    placeholder = { Text("Ex: PA, razão, termo geral, exemplos") },
                    minLines = 3, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Como será a aula", Icons.Default.FormatListBulleted) {
                OutlinedTextField(
                    value = metodologia, onValueChange = { metodologia = it },
                    label = { Text("Passo a passo") },
                    placeholder = { Text("1. Revisão rápida\n2. Explicação\n3. Duplas resolvem\n4. Correção no quadro") },
                    minLines = 5, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Materiais", Icons.Default.List) {
                ListaEditavel(
                    itens = materiais,
                    placeholder = "Ex: livro didático",
                    textoAdicionar = "Adicionar material",
                    onMudar = { i, v -> materiais = materiais.toMutableList().also { it[i] = v } },
                    onRemover = { i -> materiais = materiais.toMutableList().also { it.removeAt(i) } },
                    onAdicionar = { materiais = materiais + "" }
                )
            }

            SectionCard("Atividades", Icons.Default.Checklist) {
                ListaEditavel(
                    itens = atividades,
                    placeholder = "Ex: exercício 1 a 5 do capítulo",
                    textoAdicionar = "Adicionar atividade",
                    onMudar = { i, v -> atividades = atividades.toMutableList().also { it[i] = v } },
                    onRemover = { i -> atividades = atividades.toMutableList().also { it.removeAt(i) } },
                    onAdicionar = { atividades = atividades + "" }
                )
            }

            SectionCard("Tarefa de casa", Icons.Default.Home) {
                OutlinedTextField(
                    value = tarefa, onValueChange = { tarefa = it },
                    label = { Text("Para casa") },
                    placeholder = { Text("Ex: terminar a lista 3") },
                    minLines = 2, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Observações", Icons.Default.Edit) {
                OutlinedTextField(
                    value = observacoes, onValueChange = { observacoes = it },
                    label = { Text("Adaptações, apoio, ideia pra próxima") },
                    placeholder = { Text("Ex: chamar atenção do aluno X no início") },
                    minLines = 3, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Status", Icons.Default.Schedule) {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    STATUS_RAPIDOS.forEach { (rotulo, valor) ->
                        SelectableChip(
                            texto = rotulo,
                            selecionado = status.equals(valor, ignoreCase = true)
                        ) { status = valor }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = status, onValueChange = { status = it },
                    label = { Text("Status (pode escrever outro)") },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
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
                        data = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    mostrarData = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { mostrarData = false }) { Text("Cancelar") } }
        ) { DatePicker(state = estado) }
    }

    if (confirmarExclusao) {
        ConfirmDeleteDialog(
            titulo = "Excluir plano?",
            mensagem = "O plano de aula será apagado. O relatório da aula, se já existir, não é afetado.",
            onConfirm = {
                vm.lessonPlans.value.firstOrNull { it.id == planId }?.let { vm.deleteLessonPlan(it) }
                confirmarExclusao = false
                navController.popBackStack()
            },
            onDismiss = { confirmarExclusao = false }
        )
    }
}

/** Lista de itens que o professor adiciona e remove livremente. */
@Composable
private fun ListaEditavel(
    itens: List<String>,
    placeholder: String,
    textoAdicionar: String,
    onMudar: (Int, String) -> Unit,
    onRemover: (Int) -> Unit,
    onAdicionar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itens.forEachIndexed { index, valor ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = valor,
                    onValueChange = { onMudar(index, it) },
                    placeholder = { Text(placeholder) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onRemover(index) }) {
                    Icon(Icons.Default.Close, "Remover",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        TextButton(onClick = onAdicionar) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(6.dp))
            Text(textoAdicionar, fontWeight = FontWeight.Medium)
        }
    }
}
