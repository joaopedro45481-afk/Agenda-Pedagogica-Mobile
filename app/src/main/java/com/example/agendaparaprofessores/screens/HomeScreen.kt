package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.LessonPlan
import com.example.agendaparaprofessores.data.LessonReport
import com.example.agendaparaprofessores.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle as EstiloTexto
import java.util.Locale

/* ============================================================
 *  HOME — visual novo (lavanda + gradiente violeta + brilho)
 *  Autossuficiente: nada aqui depende de Components.kt.
 * ============================================================ */

private val PT_BR = Locale("pt", "BR")

/** Gradiente de assinatura dos botões. */
private val GradienteVioleta = listOf(VioletaProfundo, Violeta, VioletaClaro)

private fun diaSemana(d: LocalDate) =
    d.dayOfWeek.getDisplayName(EstiloTexto.FULL, PT_BR).uppercase(PT_BR)

private fun dataLonga(d: LocalDate) =
    "${d.dayOfMonth} de ${d.month.getDisplayName(EstiloTexto.FULL, PT_BR)}"

private fun dataCurta(d: LocalDate) = "%02d/%02d".format(d.dayOfMonth, d.monthValue)

/** Data do plano, ou null se ele não tem data marcada. */
private fun dataDoPlano(p: LessonPlan): LocalDate? {
    val dia = p.plannedDay ?: return null
    val mes = p.plannedMonth ?: return null
    val ano = p.plannedYear ?: return null
    return runCatching { LocalDate.of(ano, mes, dia) }.getOrNull()
}

private fun statusPlano(s: String) = s.trim().lowercase(Locale.ROOT)
private fun estaMinistrada(s: String) = statusPlano(s).startsWith("ministr")
private fun estaAdiada(s: String) = statusPlano(s).startsWith("adiad")

private fun rotuloStatus(s: String): String = when {
    estaMinistrada(s) -> "Ministrada"
    estaAdiada(s) -> "Adiada"
    statusPlano(s).startsWith("planej") -> "Planejada"
    else -> s.trim().replaceFirstChar { it.uppercase() }.ifBlank { "Planejada" }
}

private fun corDoStatus(s: String) = when {
    estaMinistrada(s) -> VerdeTexto
    estaAdiada(s) -> AmbarTexto
    else -> Violeta
}

/* ============================================================
 *  BRILHO — a camada "vidro" dos botões
 *  Faixa de luz diagonal que atravessa o gradiente.
 * ============================================================ */
@Composable
private fun BoxScope.BrilhoDiagonal(forca: Float = 0.22f) {
    Box(
        Modifier.matchParentSize().background(
            Brush.linearGradient(
                0.00f to Color.Transparent,
                0.45f to Color.White.copy(alpha = 0f),
                0.54f to Color.White.copy(alpha = forca),
                0.64f to Color.White.copy(alpha = 0f),
                1.00f to Color.Transparent
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, vm: AppViewModel = viewModel()) {
    val relatorios by vm.reports.collectAsState()
    val materias by vm.subjects.collectAsState()
    val turmas by vm.classes.collectAsState()
    val planos by vm.lessonPlans.collectAsState()

    var busca by remember { mutableStateOf("") }
    var mostrarMais by remember { mutableStateOf(false) }

    val hoje = remember { LocalDate.now() }

    val nomeMateria: (Long?) -> String? = { id -> materias.firstOrNull { it.id == id }?.name }
    val nomeTurma: (Long?) -> String? = { id -> turmas.firstOrNull { it.id == id }?.name }

    // ---- Planos de hoje (os únicos com data = hoje) ----
    val planosHoje = planos
        .filter { dataDoPlano(it) == hoje }
        .sortedBy { it.createdAt }

    // ---- Próxima aula: o plano em aberto mais perto de hoje ----
    val proximo = planos
        .filter { !estaMinistrada(it.status) }
        .let { abertos ->
            abertos.mapNotNull { p -> dataDoPlano(p)?.let { d -> p to d } }
                .filter { it.second >= hoje }
                .minByOrNull { it.second }
                ?.first
                ?: abertos.maxByOrNull { it.updatedAt }
        }

    // ---- Busca local (não toca no banco) ----
    val buscando = busca.isNotBlank()
    val planosAchados =
        if (!buscando) emptyList()
        else planos.filter { p ->
            listOfNotNull(p.title, p.objective, p.content, nomeMateria(p.subjectId), nomeTurma(p.classId))
                .any { it.contains(busca, ignoreCase = true) }
        }
    val relatoriosAchados =
        if (!buscando) emptyList()
        else relatorios.filter { r ->
            listOfNotNull(r.title, r.summary, nomeMateria(r.subjectId), nomeTurma(r.classId))
                .any { it.contains(busca, ignoreCase = true) }
        }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(FundoLavandaTopo, FundoLavanda, FundoLavandaBase, Color.White)
            )
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                Box(
                    Modifier
                        .size(64.dp)
                        .shadow(18.dp, CircleShape, spotColor = Violeta, ambientColor = Violeta)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(GradienteVioleta))
                        .clickable { navController.navigate("plano_novo") },
                    contentAlignment = Alignment.Center
                ) {
                    BrilhoDiagonal(forca = 0.30f)
                    Icon(
                        Icons.Default.Add, "Preparar uma aula", tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            bottomBar = {
                BarraInferior(
                    onTurmas = { navController.navigate("classes") },
                    onAvaliacoes = { navController.navigate("avaliacoes") },
                    onMais = { mostrarMais = true }
                )
            }
        ) { pad ->
            Column(
                Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(2.dp))
                TopoData(hoje)
                CampoBusca(busca) { busca = it }

                if (buscando) {
                    if (planosAchados.isEmpty() && relatoriosAchados.isEmpty()) {
                        CartaoBusca("Resultados", 0) {
                            Text(
                                "Nada encontrado para \"$busca\".",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSecundario,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        if (planosAchados.isNotEmpty()) {
                            CartaoBusca("Planos de aula", planosAchados.size) {
                                planosAchados.forEachIndexed { i, p ->
                                    LinhaPlano(p, nomeMateria(p.subjectId), nomeTurma(p.classId)) {
                                        navController.navigate("planos")
                                    }
                                    if (i != planosAchados.lastIndex)
                                        HorizontalDivider(Modifier.padding(horizontal = 18.dp), color = BordaSuave)
                                }
                            }
                        }
                        if (relatoriosAchados.isNotEmpty()) {
                            CartaoBusca("Relatórios", relatoriosAchados.size) {
                                relatoriosAchados.forEachIndexed { i, r ->
                                    LinhaRelatorio(r, nomeMateria(r.subjectId), nomeTurma(r.classId)) {
                                        navController.navigate("report_form/${r.id}")
                                    }
                                    if (i != relatoriosAchados.lastIndex)
                                        HorizontalDivider(Modifier.padding(horizontal = 18.dp), color = BordaSuave)
                                }
                            }
                        }
                    }
                } else {
                    CartaoProximaAula(
                        plano = proximo,
                        materia = nomeMateria(proximo?.subjectId),
                        turma = nomeTurma(proximo?.classId),
                        onClick = { navController.navigate("planos") }
                    )

                    CartaoHoje(
                        planos = planosHoje,
                        nomeMateria = nomeMateria,
                        nomeTurma = nomeTurma,
                        onVerTodos = { navController.navigate("planos") },
                        onAbrir = { navController.navigate("planos") }
                    )

                    AcoesRapidas(
                        onPlanejar = { navController.navigate("plano_novo") },
                        onNotas = { navController.navigate("avaliacoes") },
                        onRelatorio = { navController.navigate("report_form") }
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        if (mostrarMais) {
            ModalBottomSheet(
                onDismissRequest = { mostrarMais = false },
                containerColor = SuperficieBranca
            ) {
                Column(Modifier.padding(bottom = 28.dp)) {
                    Text(
                        "Mais",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextoPrincipal,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    ItemMais(Icons.Default.Edit, "Preparador de aulas", "${planos.size} planos") {
                        mostrarMais = false; navController.navigate("planos")
                    }
                    ItemMais(Icons.Default.MenuBook, "Matérias", "${materias.size} cadastradas") {
                        mostrarMais = false; navController.navigate("subjects")
                    }
                    ItemMais(Icons.Default.Assignment, "Relatórios", "${relatorios.size} no total") {
                        mostrarMais = false; navController.navigate("reports")
                    }
                    ItemMais(Icons.Default.ShowChart, "Desempenho da turma", "Evolução e médias") {
                        mostrarMais = false; navController.navigate("desempenho")
                    }
                    ItemMais(Icons.Default.Person, "Desempenho por aluno", "Nota a nota") {
                        mostrarMais = false; navController.navigate("desempenho_alunos")
                    }
                }
            }
        }
    }
}

/* =====================  TOPO (dia + data)  ===================== */

@Composable
private fun TopoData(hoje: LocalDate) {
    Column {
        Text(
            diaSemana(hoje),
            style = MaterialTheme.typography.labelLarge,
            color = Violeta,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            dataLonga(hoje),
            style = MaterialTheme.typography.headlineMedium,
            color = TextoPrincipal,
            fontWeight = FontWeight.Bold
        )
    }
}

/* =====================  BUSCA  ===================== */

@Composable
private fun CampoBusca(valor: String, onMudar: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = onMudar,
        singleLine = true,
        placeholder = { Text("Buscar aula, turma...", color = TextoSuave) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextoSuave) },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onMudar("") }) {
                    Icon(Icons.Default.Close, "Limpar", tint = TextoSuave)
                }
            }
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SuperficieBranca,
            unfocusedContainerColor = SuperficieBranca,
            focusedBorderColor = Violeta,
            unfocusedBorderColor = BordaSuave,
            focusedTextColor = TextoPrincipal,
            unfocusedTextColor = TextoPrincipal,
            cursorColor = Violeta
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

/* =====================  HERO — PRÓXIMA AULA  ===================== */

@Composable
private fun CartaoProximaAula(
    plano: LessonPlan?,
    materia: String?,
    turma: String?,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(26.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(7.dp, forma, spotColor = Violeta, ambientColor = Violeta)
            .clip(forma)
            .background(Brush.linearGradient(GradienteVioleta))
            .clickable(onClick = onClick)
    ) {
        // brilho diagonal (igual ao mockup)
        BrilhoDiagonal(forca = 0.16f)
        Column(Modifier.padding(20.dp)) {
            Text(
                "PRÓXIMA AULA",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 1.4.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                plano?.title?.ifBlank { "Aula sem título" } ?: "Nenhuma aula planejada",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (plano == null) "Toque no + para preparar sua próxima aula"
                else listOfNotNull(materia, turma).joinToString(" · ")
                    .ifBlank { "Sem matéria e turma definidas" },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (plano != null) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PilulaHero(rotuloStatus(plano.status))
                    Spacer(Modifier.weight(1f))
                    PilulaHero(
                        plano.durationMinutes?.let { "$it min" }
                            ?: dataDoPlano(plano)?.let { dataCurta(it) }
                            ?: "sem data"
                    )
                }
            }
        }
    }
}

@Composable
private fun PilulaHero(texto: String) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.20f))
            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge,
            color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

/* =====================  CARD "HOJE"  ===================== */

@Composable
private fun CartaoHoje(
    planos: List<LessonPlan>,
    nomeMateria: (Long?) -> String?,
    nomeTurma: (Long?) -> String?,
    onVerTodos: () -> Unit,
    onAbrir: (LessonPlan) -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBranca),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(vertical = 18.dp)) {
            Row(
                Modifier.padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hoje", style = MaterialTheme.typography.titleLarge, color = TextoPrincipal)
                if (planos.isNotEmpty()) {
                    Spacer(Modifier.width(10.dp))
                    BadgeContagem(planos.size)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "Ver tudo",
                    style = MaterialTheme.typography.labelLarge,
                    color = Violeta,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onVerTodos).padding(4.dp)
                )
            }
            Spacer(Modifier.height(4.dp))

            if (planos.isEmpty()) {
                Text(
                    "Nenhuma aula planejada para hoje.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            } else {
                planos.forEachIndexed { i, p ->
                    LinhaPlano(p, nomeMateria(p.subjectId), nomeTurma(p.classId)) { onAbrir(p) }
                    if (i != planos.lastIndex) {
                        HorizontalDivider(Modifier.padding(horizontal = 18.dp), color = BordaSuave)
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeContagem(n: Int) {
    Box(
        Modifier.size(26.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(VioletaProfundo, VioletaClaro))),
        contentAlignment = Alignment.Center
    ) {
        Text("$n", style = MaterialTheme.typography.labelMedium,
            color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LinhaPlano(
    plano: LessonPlan,
    materia: String?,
    turma: String?,
    onClick: () -> Unit
) {
    val meta = listOfNotNull(materia, turma).joinToString(" · ").ifBlank { "Sem matéria e turma" }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusBolinha(plano.status)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(meta, style = MaterialTheme.typography.titleMedium, color = TextoPrincipal,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(plano.title.ifBlank { "Aula sem título" },
                style = MaterialTheme.typography.bodyMedium, color = TextoSecundario,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Text(rotuloStatus(plano.status), style = MaterialTheme.typography.labelMedium,
            color = corDoStatus(plano.status), fontWeight = FontWeight.SemiBold)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = TextoSuave, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun StatusBolinha(status: String) {
    when {
        estaMinistrada(status) -> Box(
            Modifier.size(28.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(VioletaProfundo, VioletaClaro))),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp)) }

        estaAdiada(status) -> Box(
            Modifier.size(28.dp).clip(CircleShape).background(AmbarFundo)
                .border(2.dp, AmbarTexto, CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Schedule, null, tint = AmbarTexto, modifier = Modifier.size(14.dp)) }

        else -> Box(
            Modifier.size(28.dp).clip(CircleShape).background(SuperficieBranca)
                .border(2.dp, corDoStatus(status), CircleShape),
            contentAlignment = Alignment.Center
        ) { Box(Modifier.size(10.dp).clip(CircleShape).background(corDoStatus(status))) }
    }
}

/* =====================  BUSCA — resultado de relatório  ===================== */

@Composable
private fun LinhaRelatorio(
    report: LessonReport,
    materia: String?,
    turma: String?,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(VioletaWash),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.Description, null, tint = Violeta, modifier = Modifier.size(15.dp)) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(listOfNotNull(turma, materia).joinToString(" · ").ifBlank { "Relatório" },
                style = MaterialTheme.typography.titleMedium, color = TextoPrincipal,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(report.title, style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = TextoSuave, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CartaoBusca(titulo: String, quantidade: Int, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieBranca),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = TextoPrincipal)
                if (quantidade > 0) {
                    Spacer(Modifier.width(10.dp))
                    BadgeContagem(quantidade)
                }
            }
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

/* =====================  AÇÕES RÁPIDAS  ===================== */

@Composable
private fun AcoesRapidas(
    onPlanejar: () -> Unit,
    onNotas: () -> Unit,
    onRelatorio: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BotaoAcao(Icons.Default.Edit, "Planejar", true, Modifier.weight(1f), onPlanejar)
        BotaoAcao(Icons.Default.Assessment, "Lançar notas", false, Modifier.weight(1f), onNotas)
        BotaoAcao(Icons.Default.Description, "Novo relatório", false, Modifier.weight(1f), onRelatorio)
    }
}

@Composable
private fun BotaoAcao(
    icone: ImageVector,
    texto: String,
    destaque: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(18.dp)
    val tinta = if (destaque) Color.White else Violeta

    Box(
        modifier
            .height(76.dp)
            .then(
                if (destaque) Modifier
                    .shadow(14.dp, forma, spotColor = Violeta, ambientColor = Violeta)
                    .clip(forma)
                    .background(Brush.linearGradient(GradienteVioleta))
                else Modifier
                    .clip(forma)
                    .background(SuperficieBranca)
                    .border(1.dp, BordaSuave, forma)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (destaque) BrilhoDiagonal(forca = 0.28f)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icone, null, tint = tinta, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium, color = tinta,
                fontWeight = FontWeight.SemiBold, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

/* =====================  BARRA INFERIOR  ===================== */

@Composable
private fun BarraInferior(
    onTurmas: () -> Unit,
    onAvaliacoes: () -> Unit,
    onMais: () -> Unit
) {
    Column {
        HorizontalDivider(color = BordaSuave)
        NavigationBar(containerColor = SuperficieBranca, tonalElevation = 0.dp) {
            NavigationBarItem(
                selected = true, onClick = { },
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("Início") },
                colors = coresDaBarra()
            )
            NavigationBarItem(
                selected = false, onClick = onTurmas,
                icon = { Icon(Icons.Default.Groups, null) },
                label = { Text("Turmas") },
                colors = coresDaBarra()
            )
            NavigationBarItem(
                selected = false, onClick = onAvaliacoes,
                icon = { Icon(Icons.Default.Assessment, null) },
                label = { Text("Avaliações") },
                colors = coresDaBarra()
            )
            NavigationBarItem(
                selected = false, onClick = onMais,
                icon = { Icon(Icons.Default.MoreHoriz, null) },
                label = { Text("Mais") },
                colors = coresDaBarra()
            )
        }
    }
}

@Composable
private fun coresDaBarra() = NavigationBarItemDefaults.colors(
    selectedIconColor = Violeta,
    selectedTextColor = Violeta,
    indicatorColor = VioletaWash,
    unselectedIconColor = TextoSuave,
    unselectedTextColor = TextoSuave
)

@Composable
private fun ItemMais(icone: ImageVector, titulo: String, sub: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = { Text(sub) },
        leadingContent = {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(VioletaWash),
                contentAlignment = Alignment.Center
            ) { Icon(icone, null, tint = Violeta, modifier = Modifier.size(22.dp)) }
        },
        colors = ListItemDefaults.colors(containerColor = SuperficieBranca),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
