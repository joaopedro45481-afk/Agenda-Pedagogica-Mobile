package com.example.agendaparaprofessores.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.NotaAlunoProva
import com.example.agendaparaprofessores.ui.AppTopBar
import com.example.agendaparaprofessores.ui.EmptyState
import com.example.agendaparaprofessores.ui.InitialAvatar
import com.example.agendaparaprofessores.ui.SelectableChip
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

private data class AlunoResumo(
    val studentId: Long,
    val nome: String,
    val notas: List<NotaAlunoProva>,
    val mediaGeral: Double,
    val porBimestre: List<Pair<Int, Double>>,
    val evolucao: Double?
)

private data class Tendencia(
    val cor: Color,
    val texto: String,
    val icone: ImageVector
)

private fun montarResumos(notas: List<NotaAlunoProva>): List<AlunoResumo> =
    notas
        .groupBy { it.studentId }
        .map { (studentId, lista) ->
            val porBimestre = lista
                .groupBy { it.bimester }
                .map { (bimestre, doBimestre) ->
                    bimestre to doBimestre.map { it.score }.average()
                }
                .sortedBy { it.first }

            AlunoResumo(
                studentId = studentId,
                nome = lista.first().studentName,
                notas = lista.sortedWith(
                    compareBy({ it.year }, { it.month }, { it.day }, { it.assessmentId })
                ),
                mediaGeral = lista.map { it.score }.average(),
                porBimestre = porBimestre,
                evolucao = if (porBimestre.size >= 2) {
                    porBimestre.last().second - porBimestre.first().second
                } else {
                    null
                }
            )
        }

@Composable
private fun tendenciaDe(evolucao: Double?): Tendencia = when {
    evolucao == null -> Tendencia(
        MaterialTheme.colorScheme.onSurfaceVariant, "—", Icons.Default.Remove
    )
    evolucao >= 0.5 -> Tendencia(VerdeTexto, "+${formatarNumero(evolucao)}", Icons.Default.ArrowUpward)
    evolucao <= -0.5 -> Tendencia(VermelhoTexto, formatarNumero(evolucao), Icons.Default.ArrowDownward)
    else -> Tendencia(AmbarTexto, "estável", Icons.Default.Remove)
}

private fun formatarNumero(valor: Double): String =
    if (valor == valor.roundToInt().toDouble()) valor.roundToInt().toString()
    else "%.2f".format(valor).replace('.', ',')

private fun dataDaProva(nota: NotaAlunoProva): String =
    "%02d/%02d/%04d".format(nota.day, nota.month, nota.year)

/* ============================================================
 *  TELA
 * ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPerformanceScreen(
    navController: NavHostController,
    turmaInicial: Long = -1L,
    vm: AppViewModel = viewModel()
) {
    val turmas by vm.classes.collectAsState()

    var turmaSelecionada by remember(turmaInicial) { mutableStateOf(turmaInicial) }
    var bimestreFiltro by remember { mutableStateOf(0) }   // 0 = todos
    var ordenacao by remember { mutableStateOf(0) }        // 0 nome · 1 média · 2 evolução
    var alunoDetalhe by remember { mutableStateOf<AlunoResumo?>(null) }

    // Se veio sem turma definida, pega a primeira
    LaunchedEffect(turmas) {
        if (turmaSelecionada == -1L && turmas.isNotEmpty()) {
            turmaSelecionada = turmas.first().id
        }
    }

    val notas by remember(turmaSelecionada) {
        vm.notasDaTurma(turmaSelecionada)
    }.collectAsState(initial = emptyList<NotaAlunoProva>())

    val notasFiltradas = remember(notas, bimestreFiltro) {
        if (bimestreFiltro == 0) notas else notas.filter { it.bimester == bimestreFiltro }
    }

    val alunos = remember(notasFiltradas, ordenacao) {
        val base = montarResumos(notasFiltradas)
        when (ordenacao) {
            1 -> base.sortedByDescending { it.mediaGeral }
            2 -> base.sortedByDescending { it.evolucao ?: Double.NEGATIVE_INFINITY }
            else -> base.sortedBy { it.nome.lowercase() }
        }
    }

    val nomeTurma = turmas.firstOrNull { it.id == turmaSelecionada }?.name ?: "Escolha a turma"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = "Desempenho por aluno",
                subtitulo = nomeTurma,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (turmas.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icone = Icons.Default.Person,
                    titulo = "Nenhuma turma cadastrada",
                    mensagem = "Cadastre uma turma, adicione alunos e lance notas para comparar."
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
                    text = "Turma",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    turmas.forEach { turma ->
                        SelectableChip(turma.name, turmaSelecionada == turma.id) {
                            turmaSelecionada = turma.id
                        }
                    }
                }

                Text(
                    text = "Bimestre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip("Todos", bimestreFiltro == 0) { bimestreFiltro = 0 }
                    (1..4).forEach { i ->
                        SelectableChip("${i}º", bimestreFiltro == i) { bimestreFiltro = i }
                    }
                }

                Text(
                    text = "Ordenar por",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableChip("Nome", ordenacao == 0) { ordenacao = 0 }
                    SelectableChip("Média", ordenacao == 1) { ordenacao = 1 }
                    SelectableChip("Evolução", ordenacao == 2) { ordenacao = 2 }
                }

                if (alunos.isEmpty()) {
                    EmptyState(
                        modifier = Modifier.fillMaxWidth(),
                        icone = Icons.Default.ShowChart,
                        titulo = "Sem notas ainda",
                        mensagem = "Lance as notas em Avaliações para comparar os alunos " +
                                "entre os bimestres."
                    )
                } else {
                    Text(
                        text = "${alunos.size} aluno(s) com nota lançada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    alunos.forEach { aluno ->
                        CartaoAluno(
                            aluno = aluno,
                            bimestreFiltro = bimestreFiltro,
                            onClick = { alunoDetalhe = it }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    alunoDetalhe?.let { aluno ->
        ModalBottomSheet(
            onDismissRequest = { alunoDetalhe = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DetalheAluno(aluno)
        }
    }
}

/* ============================================================
 *  COMPONENTES
 * ============================================================ */

@Composable
private fun CartaoAluno(
    aluno: AlunoResumo,
    bimestreFiltro: Int,
    onClick: (AlunoResumo) -> Unit
) {
    val tendencia = tendenciaDe(aluno.evolucao)
    val rotuloMedia = if (bimestreFiltro == 0) "média" else "média do ${bimestreFiltro}º bim"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(aluno) },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InitialAvatar(aluno.nome, Roxo)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = aluno.nome,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$rotuloMedia ${formatarNumero(aluno.mediaGeral)} • " +
                                "${aluno.notas.size} prova(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EtiquetaTendencia(tendencia)
            }

            Spacer(Modifier.height(12.dp))
            MiniGrafico(valores = aluno.porBimestre.map { it.second }, cor = tendencia.cor)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aluno.porBimestre.forEach { (bimestre, media) ->
                    PillBimestre(bimestre, media)
                }
            }
        }
    }
}

@Composable
private fun EtiquetaTendencia(tendencia: Tendencia) {
    Row(
        modifier = Modifier
            .background(tendencia.cor.copy(alpha = 0.14f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = tendencia.icone,
            contentDescription = null,
            tint = tendencia.cor,
            modifier = Modifier.height(14.dp).width(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = tendencia.texto,
            style = MaterialTheme.typography.labelMedium,
            color = tendencia.cor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PillBimestre(bimestre: Int, media: Double) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = "${bimestre}º bim",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = formatarNumero(media),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Linha compacta: escala 0 a 10. Cores lidas FORA do Canvas. */
@Composable
private fun MiniGrafico(valores: List<Double>, cor: Color) {
    if (valores.isEmpty()) return

    val corGrade = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        val passo = if (valores.size <= 1) 0f else size.width / (valores.size - 1).toFloat()
        fun x(i: Int) = if (valores.size <= 1) size.width / 2f else i * passo
        fun y(v: Double) = size.height - (v / 10.0).coerceIn(0.0, 1.0).toFloat() * size.height

        drawLine(
            color = corGrade,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1f
        )

        if (valores.size > 1) {
            val caminho = Path()
            caminho.moveTo(x(0), y(valores[0]))
            for (i in 1 until valores.size) {
                caminho.lineTo(x(i), y(valores[i]))
            }
            drawPath(caminho, cor, style = Stroke(width = 4f, cap = StrokeCap.Round))
        }

        valores.indices.forEach { i ->
            drawCircle(color = cor, radius = 4f, center = Offset(x(i), y(valores[i])))
        }
    }
}

/** Gráfico maior do detalhe: escala 0 a 10 com rótulos. */
@Composable
private fun GraficoBimestres(valores: List<Double>, rotulos: List<String>) {
    if (valores.isEmpty()) return

    val corGrade = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val corPonto = MaterialTheme.colorScheme.surface
    val corLinha = RoxoMedio

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val margemHorizontal = 16f
            val margemVertical = 14f
            val larguraUtil = size.width - margemHorizontal * 2
            val alturaUtil = size.height - margemVertical * 2

            fun pontoDe(i: Int): Offset {
                val x = if (valores.size <= 1) {
                    size.width / 2f
                } else {
                    margemHorizontal + i * larguraUtil / (valores.size - 1).toFloat()
                }
                val normalizado = (valores[i] / 10.0).coerceIn(0.0, 1.0)
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
                val primeiro = pontoDe(0)
                caminho.moveTo(primeiro.x, primeiro.y)
                for (i in 1 until valores.size) {
                    val atual = pontoDe(i)
                    caminho.lineTo(atual.x, atual.y)
                }
                drawPath(caminho, corLinha, style = Stroke(width = 5f, cap = StrokeCap.Round))
            }

            for (i in valores.indices) {
                val p = pontoDe(i)
                drawCircle(color = corPonto, radius = 8f, center = p)
                drawCircle(color = corLinha, radius = 5f, center = p)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            rotulos.forEach { rotulo ->
                Text(
                    text = rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetalheAluno(aluno: AlunoResumo) {
    val tendencia = tendenciaDe(aluno.evolucao)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 560.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InitialAvatar(aluno.nome, Roxo)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(aluno.nome, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "média geral ${formatarNumero(aluno.mediaGeral)} • " +
                            "${aluno.notas.size} prova(s) lançada(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EtiquetaTendencia(tendencia)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Média por bimestre",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        GraficoBimestres(
            valores = aluno.porBimestre.map { it.second },
            rotulos = aluno.porBimestre.map { "${it.first}º bim" }
        )

        if (aluno.evolucao == null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Só há notas em um bimestre — lance notas em outro bimestre " +
                        "para ver a evolução.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(22.dp))
        Text(
            text = "Provas lançadas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))

        aluno.notas.forEach { nota ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = nota.assessmentTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${dataDaProva(nota)} • ${nota.bimester}º bimestre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = formatarNumero(nota.score),
                    style = MaterialTheme.typography.titleMedium,
                    color = Roxo,
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}
