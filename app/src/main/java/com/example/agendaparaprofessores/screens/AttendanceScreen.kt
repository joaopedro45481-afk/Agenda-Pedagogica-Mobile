package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.PresencaAluno
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Chamada: escolhe turma + dia, desmarca quem faltou e salva.
 * Quem não tem registro começa como PRESENTE.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavHostController,
    turmaInicial: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val turmas by vm.classes.collectAsState()

    var turmaId by remember {
        mutableStateOf<Long?>(if (turmaInicial > 0L) turmaInicial else null)
    }
    var data by remember { mutableStateOf(LocalDate.now()) }
    var mostrarData by remember { mutableStateOf(false) }

    var alunos by remember { mutableStateOf<List<PresencaAluno>>(emptyList()) }
    val presencas = remember { mutableStateMapOf<Long, Boolean>() }
    var carregando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val turma = turmas.firstOrNull { it.id == turmaId }

    // Carrega só quando muda turma ou data — não apaga o que você já marcou.
    LaunchedEffect(turmaId, data) {
        val id = turmaId
        if (id == null) {
            alunos = emptyList(); presencas.clear(); return@LaunchedEffect
        }
        carregando = true
        val lista = vm.chamadaDaTurma(id, data.dayOfMonth, data.monthValue, data.year).first()
        alunos = lista
        presencas.clear()
        lista.forEach { presencas[it.studentId] = it.present }
        carregando = false
    }

    val presentes = alunos.count { presencas[it.studentId] != false }
    val faltas = alunos.size - presentes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AppTopBar(
                titulo = "Chamada",
                subtitulo = turma?.name ?: "Escolha a turma",
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            if (turmaId != null && alunos.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                    Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                        Button(
                            onClick = {
                                val id = turmaId ?: return@Button
                                val mapa = alunos.associate {
                                    it.studentId to (presencas[it.studentId] ?: true)
                                }
                                scope.launch {
                                    vm.salvarChamada(
                                        id, data.dayOfMonth, data.monthValue, data.year, mapa
                                    )
                                    snackbar.showSnackbar("Chamada salva")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Salvar • $presentes presentes",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard("Turma e data", Icons.Default.Groups) {
                    DropdownField(
                        label = "Turma",
                        itens = turmas.map { it.id to it.name },
                        selecionadoId = turmaId,
                        placeholder = "Selecione a turma"
                    ) { turmaId = it }
                    Spacer(Modifier.height(12.dp))
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
                }
            }

            when {
                turmaId == null -> item {
                    EmptyState(
                        Icons.Default.Groups, "Escolha uma turma",
                        "A lista de alunos aparece aqui depois que você escolher a turma e o dia."
                    )
                }

                carregando -> item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                alunos.isEmpty() -> item {
                    EmptyState(
                        Icons.Default.Person, "Nenhum aluno nessa turma",
                        "Cadastre os alunos da turma primeiro (Turmas → Adicionar alunos)."
                    )
                }

                else -> {
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ContadorChamada("$presentes", "presentes", VerdeTexto)
                                Spacer(Modifier.width(10.dp))
                                ContadorChamada(
                                    "$faltas", "faltas",
                                    if (faltas > 0) VermelhoTexto else TextoSecundario
                                )
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = {
                                    alunos.forEach { presencas[it.studentId] = true }
                                }) { Text("Todos presentes") }
                            }
                        }
                    }

                    items(alunos, key = { it.studentId }) { a ->
                        LinhaChamada(
                            nome = a.nome,
                            presente = presencas[a.studentId] ?: true
                        ) { marcado -> presencas[a.studentId] = marcado }
                    }
                }
            }
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
}

@Composable
private fun ContadorChamada(valor: String, rotulo: String, cor: Color) {
    Column(
        Modifier.clip(RoundedCornerShape(14.dp))
            .background(cor.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(valor, style = MaterialTheme.typography.titleLarge, color = cor,
            fontWeight = FontWeight.Bold)
        Text(rotulo, style = MaterialTheme.typography.labelSmall, color = cor)
    }
}

/** Caixinha marcada = presente. Toque na linha ou na caixinha pra alternar. */
@Composable
private fun LinhaChamada(
    nome: String,
    presente: Boolean,
    onMudar: (Boolean) -> Unit
) {
    Card(
        Modifier.fillMaxWidth().clickable { onMudar(!presente) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = presente, onCheckedChange = onMudar)
            Spacer(Modifier.width(6.dp))
            Text(
                nome,
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (presente) TextoPrincipal else TextoSuave
            )
            Text(
                if (presente) "Presente" else "Faltou",
                style = MaterialTheme.typography.labelMedium,
                color = if (presente) VerdeTexto else VermelhoTexto,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(10.dp))
        }
    }
}
