package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.Assessment
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun bimestrePorMes(mes: Int) = when (mes) {
    in 2..4 -> 1
    in 5..6 -> 2
    in 7..9 -> 3
    else -> 4
}

private fun formatarNota(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else v.toString().replace('.', ',')

private fun parseNota(texto: String): Double? {
    val t = texto.trim().replace(',', '.')
    if (t.isEmpty()) return null
    val v = t.toDoubleOrNull() ?: return null
    return if (v in 0.0..10.0) v else null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentFormScreen(
    navController: NavHostController,
    assessmentId: Long = 0L,
    initialClassId: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val escopo = rememberCoroutineScope()
    val turmas by vm.classes.collectAsState()

    var editando by remember { mutableStateOf(assessmentId != 0L) }
    var turmaId by remember { mutableStateOf(if (initialClassId > 0L) initialClassId else -1L) }
    var titulo by remember { mutableStateOf("") }
    var data by remember { mutableStateOf(LocalDate.now()) }
    var bimestre by remember { mutableStateOf(bimestrePorMes(LocalDate.now().monthValue)) }
    var notas by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var erro by remember { mutableStateOf<String?>(null) }
    var mostrarData by remember { mutableStateOf(false) }
    var confirmarExclusao by remember { mutableStateOf(false) }
    var salvando by remember { mutableStateOf(false) }

    LaunchedEffect(assessmentId) {
        if (assessmentId != 0L) {
            vm.getAssessment(assessmentId)?.let { a ->
                editando = true
                turmaId = a.classId
                titulo = a.title
                data = LocalDate.of(a.year, a.month, a.day)
                bimestre = a.bimester
            }
        }
    }

    val alunosNotasFlow = remember(turmaId, assessmentId) {
        if (turmaId > 0L) vm.notasDaAvaliacao(turmaId, assessmentId) else flowOf(emptyList())
    }
    val alunosNotas by alunosNotasFlow.collectAsState(initial = emptyList())

    LaunchedEffect(alunosNotas) {
        if (alunosNotas.isNotEmpty()) {
            val atuais = notas.toMutableMap()
            alunosNotas.forEach { a ->
                if (!atuais.containsKey(a.studentId)) {
                    atuais[a.studentId] = a.nota?.let { formatarNota(it) } ?: ""
                }
            }
            notas = atuais
        }
    }

    val nomeTurma = turmas.firstOrNull { it.id == turmaId }?.name

    val avaliadas = notas.values.mapNotNull { parseNota(it) }
    val quantosNa = alunosNotas.count { notas[it.studentId].isNullOrBlank() }
    val media = if (avaliadas.isEmpty()) null else avaliadas.average()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = if (editando) "Editar avaliação" else "Nova avaliação",
                subtitulo = "${data.dayOfMonth}/${data.monthValue}/${data.year} • ${bimestre}º Bimestre",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                    if (alunosNotas.isNotEmpty()) {
                        Text(
                            "${avaliadas.size} de ${alunosNotas.size} avaliados • " +
                                    "$quantosNa NA • " +
                                    (media?.let { "Média ${formatarNota(it)}" } ?: "Sem média"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    Button(
                        enabled = !salvando,
                        onClick = {
                            val invalidas = notas.filter { (_, t) ->
                                t.isNotBlank() && parseNota(t) == null
                            }
                            when {
                                turmaId <= 0L -> erro = "Escolha a turma"
                                titulo.isBlank() -> erro = "Escreva o nome da avaliação"
                                invalidas.isNotEmpty() ->
                                    erro = "Alguma nota está fora de 0 a 10. Corrija ou deixe em branco (NA)."
                                else -> {
                                    salvando = true
                                    escopo.launch {
                                        vm.salvarAvaliacaoComNotas(
                                            Assessment(
                                                id = assessmentId,
                                                classId = turmaId,
                                                title = titulo.trim(),
                                                day = data.dayOfMonth,
                                                month = data.monthValue,
                                                year = data.year,
                                                bimester = bimestre
                                            ),
                                            notas.mapValues { (_, t) -> parseNota(t) }
                                        )
                                        navController.popBackStack()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            if (editando) "Salvar alterações" else "Salvar avaliação",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (editando) {
                        TextButton(
                            onClick = { confirmarExclusao = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Excluir avaliação", color = MaterialTheme.colorScheme.error) }
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

            SectionCard("Avaliação", Icons.Default.Assessment) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it; erro = null },
                    label = { Text("Nome da avaliação") },
                    placeholder = { Text("Ex: Prova de Progressão Aritmética") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                DropdownField(
                    label = "Turma",
                    itens = turmas.map { it.id to it.name },
                    selecionadoId = if (turmaId > 0L) turmaId else null,
                    placeholder = "Selecione a turma"
                ) { turmaId = it; erro = null; notas = emptyMap() }
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

            when {
                turmaId <= 0L -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Escolha a turma acima que os alunos aparecem aqui para lançar as notas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                alunosNotas.isEmpty() -> {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EmptyState(
                            Icons.Default.Person,
                            "Turma sem alunos",
                            "${nomeTurma ?: "Essa turma"} ainda não tem alunos cadastrados.",
                            action = {
                                Button(
                                    onClick = { navController.navigate("alunos/$turmaId") },
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Adicionar alunos") }
                            }
                        )
                    }
                }

                else -> {
                    SectionCard("Notas da turma", Icons.Default.School) {
                        Text(
                            "Deixe em branco para NA (não avaliado).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        alunosNotas.forEach { a ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InitialAvatar(a.nome, RoxoMedio)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    a.nome,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = notas[a.studentId].orEmpty(),
                                    onValueChange = { novo ->
                                        val limpo = novo
                                            .filter { it.isDigit() || it == ',' || it == '.' }
                                            .take(4)
                                        notas = notas + (a.studentId to limpo)
                                        erro = null
                                    },
                                    singleLine = true,
                                    placeholder = { Text("NA", textAlign = TextAlign.Center) },
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.width(84.dp)
                                )
                            }
                        }
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
                        bimestre = bimestrePorMes(d.monthValue)
                    }
                    mostrarData = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarData = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = estado) }
    }

    if (confirmarExclusao) {
        ConfirmDeleteDialog(
            titulo = "Excluir avaliação?",
            mensagem = "As notas lançadas nela também serão apagadas.",
            onConfirm = {
                vm.assessments.value.firstOrNull { it.id == assessmentId }
                    ?.let { vm.deleteAvaliacao(it) }
                confirmarExclusao = false
                navController.popBackStack()
            },
            onDismiss = { confirmarExclusao = false }
        )
    }
}
