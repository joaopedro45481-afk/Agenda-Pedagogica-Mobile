package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.LessonReport
import com.example.agendaparaprofessores.ui.AppTopBar
import com.example.agendaparaprofessores.ui.EmptyState
import com.example.agendaparaprofessores.ui.SelectableChip
import com.example.agendaparaprofessores.ui.SectionCard
import com.example.agendaparaprofessores.ui.theme.AmbarTexto
import com.example.agendaparaprofessores.ui.theme.AppViewModel
import com.example.agendaparaprofessores.ui.theme.Roxo
import com.example.agendaparaprofessores.ui.theme.RoxoMedio
import com.example.agendaparaprofessores.ui.theme.VerdeTexto
import com.example.agendaparaprofessores.ui.theme.VermelhoTexto
import kotlin.math.roundToInt

/* ============================================================
 *  MODELOS E CÁLCULOS
 * ============================================================ */

private data class ResumoBimestre(
    val bimestre: Int,
    val mediaDificuldade: Double,
    val aulas: Int,
    val faceis: Int,
    val medios: Int,
    val dificeis: Int
)

/** Fácil = 3 (bom) · Médio = 2 · Difícil = 1 (ruim). */
private fun valorDificuldade(dificuldade: String): Double = when {
    dificuldade.startsWith("Fácil", true) || dificuldade.startsWith("Facil", true) -> 3.0
    dificuldade.startsWith("Difícil", true) || dificuldade.startsWith("Dificil", true) -> 1.0
    else -> 2.0
}

private fun calcularResumo(relatorios: List<LessonReport>): List<ResumoBimestre> =
    relatorios
        .groupBy { it.bimester }
        .map { (bimestre, lista) ->
            ResumoBimestre(
                bimestre = bimestre,
                mediaDificuldade = lista.map { valorDificuldade(it.difficulty) }.average(),
                aulas = lista.size,
                faceis = lista.count { valorDificuldade(it.difficulty) == 3.0 },
                medios = lista.count { valorDificuldade(it.difficulty) == 2.0 },
                dificeis = lista.count { valorDificuldade(it.difficulty) == 1.0 }
            )
        }
        .sortedBy { it.bimestre }

private fun formatarNumero(valor: Double): String =
    if (valor == valor.roundToInt().toDouble()) valor.roundToInt().toString()
    else "%.2f".format(valor).replace('.', ',')

/* ============================================================
 *  TELA
 * ============================================================ */

@Composable
fun PerformanceScreen(
    navController: NavHostController,
    turmaInicial: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val turmas by vm.classes.collectAsState()
    val relatorios by vm.reports.collectAsState()
    val mediasAvaliacoes by vm.mediasAvaliacoes.collectAsState()

    var turmaSelecionada by remember { mutableStateOf(turmaInicial) }

    val relatoriosFiltrados = remember(relatorios, turmaSelecionada) {
        if (turmaSelecionada == -1L) relatorios
        else relatorios.filter { it.classId == turmaSelecionada }
    }

    val mediasFiltradas = remember(mediasAvaliacoes, turmaSelecionada) {
        if (turmaSelecionada == -1L) mediasAvaliacoes
        else mediasAvaliacoes.filter { it.classId == turmaSelecionada }
    }

    val resumo = remember(relatoriosFiltrados) { calcularResumo(relatoriosFiltrados) }

    val primeiroResumo = resumo.firstOrNull()
    val ultimoResumo = resumo.lastOrNull()

    val diferenca = if (primeiroResumo != null && ultimoResumo != null) {
        ultimoResumo.mediaDificuldade - primeiroResumo.mediaDificuldade
    } else {
        0.0
    }

    val possuiTendencia = resumo.size >= 2

    val tituloTendencia = when {
        !possuiTendencia -> "Dados insuficientes"
        diferenca > 0.15 -> "A turma melhorou"
        diferenca < -0.15 -> "A turma piorou"
        else -> "Desempenho estável"
    }

    val iconeTendencia = when {
        !possuiTendencia -> Icons.Default.TrendingFlat
        diferenca > 0.15 -> Icons.Default.TrendingUp
        diferenca < -0.15 -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }

    val corTendencia = when {
        !possuiTendencia -> MaterialTheme.colorScheme.onSurfaceVariant
        diferenca > 0.15 -> VerdeTexto
        diferenca < -0.15 -> VermelhoTexto
        else -> AmbarTexto
    }

    val nomeTurma = turmas.firstOrNull { it.id == turmaSelecionada }?.name ?: "Todas as turmas"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = "Desempenho",
                subtitulo = nomeTurma,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (turmas.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icone = Icons.Default.Groups,
                    titulo = "Nenhuma turma cadastrada",
                    mensagem = "Cadastre uma turma e crie relatórios para visualizar o desempenho."
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Escolha a turma",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip(
                        texto = "Todas",
                        selecionado = turmaSelecionada == -1L,
                        onClick = { turmaSelecionada = -1L }
                    )
                    turmas.forEach { turma ->
                        SelectableChip(
                            texto = turma.name,
                            selecionado = turmaSelecionada == turma.id,
                            onClick = { turmaSelecionada = turma.id }
                        )
                    }
                }

                if (relatoriosFiltrados.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.fillMaxWidth(),
                        icone = Icons.Default.ShowChart,
                        titulo = "Sem dados ainda",
                        mensagem = "Crie pelo menos dois relatórios com a dificuldade da turma " +
                                "para visualizar a evolução."
                    )
                } else {
                    CartaoVeredito(
                        titulo = tituloTendencia,
                        icone = iconeTendencia,
                        cor = corTendencia,
                        subtitulo = if (possuiTendencia && primeiroResumo != null && ultimoResumo != null) {
                            "Comparação entre o ${primeiroResumo.bimestre}º e o " +
                                    "${ultimoResumo.bimestre}º bimestre"
                        } else {
                            "É necessário ter dados em pelo menos dois bimestres"
                        }
                    )

                    // ============ Atalho para o desempenho por aluno ============
                    Button(
                        onClick = {
                            navController.navigate(
                                if (turmaSelecionada == -1L) "desempenho_alunos"
                                else "desempenho_alunos/$turmaSelecionada"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Desempenho por aluno",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    SectionCard(titulo = "Evolução da turma", icone = Icons.Default.ShowChart) {
                        Text(
                            text = "Quanto maior a pontuação, mais fácil foi para a turma " +
                                    "acompanhar as aulas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        GraficoLinha(
                            valores = resumo.map { it.mediaDificuldade },
                            cor = Roxo,
                            minimo = 0.0,
                            maximo = 3.0
                        )
                        Spacer(Modifier.height(8.dp))
                        RotulosGrafico(resumo.map { "${it.bimestre}º bim." })
                    }

                    SectionCard(titulo = "Distribuição das aulas", icone = Icons.Default.Assessment) {
                        GraficoBarras(resumo)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendaGrafico("Fácil", VerdeTexto)
                            LegendaGrafico("Médio", AmbarTexto)
                            LegendaGrafico("Difícil", VermelhoTexto)
                        }
                    }

                    val mediasPorBimestre = mediasFiltradas
                        .filter { it.media != null }
                        .groupBy { it.bimester }
                        .mapNotNull { (bimestre, lista) ->
                            val valores = lista.mapNotNull { it.media }
                            if (valores.isEmpty()) null else bimestre to valores.average()
                        }
                        .sortedBy { it.first }

                    if (mediasPorBimestre.isNotEmpty()) {
                        SectionCard(titulo = "Média das avaliações", icone = Icons.Default.Assessment) {
                            Text(
                                text = "Notas médias por bimestre. Alunos com NA não entram no cálculo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            GraficoLinha(
                                valores = mediasPorBimestre.map { it.second },
                                cor = RoxoMedio,
                                minimo = 0.0,
                                maximo = 10.0
                            )
                            Spacer(Modifier.height(8.dp))
                            RotulosGrafico(mediasPorBimestre.map { "${it.first}º bim." })

                            Spacer(Modifier.height(12.dp))
                            mediasPorBimestre.forEach { (bimestre, media) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${bimestre}º bimestre",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = formatarNumero(media),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Roxo,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    SectionCard(titulo = "Resumo por bimestre", icone = Icons.Default.Assessment) {
                        resumo.forEachIndexed { index, item ->
                            LinhaResumoBimestre(item)
                            if (index < resumo.lastIndex) Spacer(Modifier.height(10.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/* ============================================================
 *  COMPONENTES
 * ============================================================ */

@Composable
private fun CartaoVeredito(
    titulo: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    cor: Color,
    subtitulo: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(cor.copy(alpha = 0.14f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = cor,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = cor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Gráfico de linha genérico.
 * IMPORTANTE: as cores são lidas FORA do Canvas — o lambda do Canvas
 * não é @Composable e não pode acessar MaterialTheme.
 */
@Composable
private fun GraficoLinha(
    valores: List<Double>,
    cor: Color,
    minimo: Double,
    maximo: Double
) {
    if (valores.isEmpty()) return

    val corFundo = MaterialTheme.colorScheme.surface
    val corGrade = Color.LightGray.copy(alpha = 0.35f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val margemHorizontal = 18f
        val margemVertical = 16f
        val larguraUtil = size.width - margemHorizontal * 2
        val alturaUtil = size.height - margemVertical * 2
        val intervalo = (maximo - minimo).coerceAtLeast(0.01)

        fun pontoDe(index: Int): Offset {
            val x = if (valores.size == 1) {
                size.width / 2f
            } else {
                margemHorizontal + index * larguraUtil / (valores.size - 1).toFloat()
            }
            val normalizado = ((valores[index] - minimo) / intervalo).coerceIn(0.0, 1.0)
            val y = size.height - margemVertical - (normalizado * alturaUtil).toFloat()
            return Offset(x, y)
        }

        for (i in 0..4) {
            val y = margemVertical + i * alturaUtil / 4f
            drawLine(
                color = corGrade,
                start = Offset(margemHorizontal, y),
                end = Offset(size.width - margemHorizontal, y),
                strokeWidth = 1f
            )
        }

        if (valores.size > 1) {
            val caminho = Path()
            val primeiroPonto = pontoDe(0)
            caminho.moveTo(primeiroPonto.x, primeiroPonto.y)
            for (i in 1 until valores.size) {
                val atual = pontoDe(i)
                caminho.lineTo(atual.x, atual.y)
            }
            drawPath(
                path = caminho,
                color = cor,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }

        for (i in valores.indices) {
            val p = pontoDe(i)
            drawCircle(color = corFundo, radius = 8f, center = p)
            drawCircle(color = cor, radius = 5f, center = p)
        }
    }
}

/** Barras Fácil/Médio/Difícil por bimestre. Cores lidas fora do Canvas. */
@Composable
private fun GraficoBarras(resumo: List<ResumoBimestre>) {
    if (resumo.isEmpty()) return

    val corFacil = VerdeTexto
    val corMedio = AmbarTexto
    val corDificil = VermelhoTexto

    val maiorQuantidade = resumo
        .maxOf { maxOf(it.faceis, it.medios, it.dificeis) }
        .coerceAtLeast(1)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    ) {
        val larguraGrupo = size.width / resumo.size
        val larguraBarra = (larguraGrupo * 0.18f).coerceAtLeast(10f)
        val espacamento = larguraBarra * 0.25f
        val alturaUtil = size.height - 20f

        resumo.forEachIndexed { index, item ->
            val centro = larguraGrupo * index + larguraGrupo / 2f
            val barras = listOf(
                item.faceis to corFacil,
                item.medios to corMedio,
                item.dificeis to corDificil
            )

            barras.forEachIndexed { barraIndex, barra ->
                val quantidade = barra.first
                val barraCor = barra.second
                val altura = quantidade.toFloat() / maiorQuantidade * alturaUtil
                val x = centro +
                        (barraIndex - 1) * (larguraBarra + espacamento) -
                        larguraBarra / 2f

                drawRoundRect(
                    color = barraCor.copy(alpha = 0.85f),
                    topLeft = Offset(x, size.height - altura),
                    size = Size(larguraBarra, altura),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }
    }
}

@Composable
private fun RotulosGrafico(textos: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        textos.forEach { texto ->
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendaGrafico(texto: String, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(cor, RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LinhaResumoBimestre(resumo: ResumoBimestre) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${resumo.bimestre}º bimestre",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "média ${formatarNumero(resumo.mediaDificuldade)}/3",
                style = MaterialTheme.typography.bodyMedium,
                color = Roxo,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = "${resumo.aulas} aula(s) • ${resumo.faceis} fácil(is) • " +
                    "${resumo.medios} média(s) • ${resumo.dificeis} difícil(eis)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
