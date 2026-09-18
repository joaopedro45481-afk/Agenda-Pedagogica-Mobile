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
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.agendaparaprofessores.data.ResumoFrequencia          // NOVO
import com.example.agendaparaprofessores.ui.AppTopBar
import com.example.agendaparaprofessores.ui.BarraGrafico             // NOVO
import com.example.agendaparaprofessores.ui.EmptyState
import com.example.agendaparaprofessores.ui.GraficoBarras            // NOVO
import com.example.agendaparaprofessores.ui.GraficoLinha
import com.example.agendaparaprofessores.ui.InitialAvatar
import com.example.agendaparaprofessores.ui.PontoGrafico
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
    val mediaGeral: Double?,
    val porBimestre: List<Pair<Int, Double>>,
    val evolucao: Double?,
    // ---- frequência (NOVO) ----
    val presentes: Int,
    val faltas: Int,
    val aulas: Int,
    val percentualPresenca: Int?   // null = sem chamada registrada
)

private data class Tendencia(
    val cor: Color,
    val texto: String,
    val icone: ImageVector
)

/** Junta as notas e a frequência pelo studentId (um aluno pode ter só uma das duas). */
private fun montarResumos(
    notas: List<NotaAlunoProva>,
    frequencias: List<ResumoFrequencia>
): List<AlunoResumo> {
    val notasPorAluno = notas.groupBy { it.studentId }
    val frequenciaPorAluno = frequencias.associateBy { it.studentId }
    val ids = (notasPorAluno.keys + frequenciaPorAluno.keys).distinct()

    return ids.map { id ->
        val lista = notasPorAluno[id].orEmpty()
        val freq = frequenciaPorAluno[id]

        val porBimestre = lista
            .groupBy { it.bimester }
            .map { (bimestre, doBimestre) ->
                bimestre to doBimestre.map { it.score }.average()
            }
            .sortedBy { it.first }

        AlunoResumo(
            studentId = id,
            nome = lista.firstOrNull()?.studentName ?: freq?.nome ?: "Aluno",
            notas = lista.sortedWith(
                compareBy({ it.year }, { it.month }, { it.day }, { it.assessmentId })
            ),
            mediaGeral = if (lista.isEmpty()) null else lista.map { it.score }.average(),
            porBimestre = porBimestre,
            evolucao = if (porBimestre.size >= 2) {
                porBimestre.last().second - porBimestre.first().second
            } else {
                null
            },
            presentes = freq?.presentes ?: 0,
            faltas = freq?.faltas ?: 0,
            aulas = freq?.total ?: 0,
            // o modelo devolve 100 quando total == 0 — aqui isso vira "sem chamada"
            percentualPresenca = freq?.takeIf { it.total > 0 }?.percentual
        )
    }
}

/** Média da turma em cada prova, em ordem cronológica. */
private fun pontosDaTurma(notas: List<NotaAlunoProva>): List<PontoGrafico> =
    notas
        .groupBy { it.assessmentId }
        .entries
        .sortedBy { (_, lista) ->
            val ref = lista.first()
            ref.year * 10000 + ref.month * 100 + ref.day
        }
        .map { (_, lista) ->
            val ref = lista.first()
            PontoGrafico(
                rotulo = "%02d/%02d".format(ref.day, ref.month),
                valor = lista.map { it.score }.average()
            )
        }

@Composable
private fun corDaPresenca(percentual: Int?): Color = when {
    percentual == null -> MaterialTheme.colorScheme.onSurfaceVariant
    percentual >= 85 -> VerdeTexto
    percentual >= 75 -> AmbarTexto
    else -> VermelhoTexto
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

private fun formatarMedia(valor: Double?): String =
    valor?.let { formatarNumero(it) } ?: "—"

private fun dataDaProva(nota: NotaAlunoProva): String =
    "%02d/%02d/%04d".format(nota.day, nota.month, nota.year)

/** "Ana Souza" -> "Ana S." (pra caber embaixo da barra). */
private fun apelido(nome: String): String {
    val partes = nome.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.isEmpty() -> "Aluno"
        partes.size == 1 -> partes[0]
        else -> "${partes[0]} ${partes[1].first().uppercaseChar()}."
    }
}

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

    LaunchedEffect(turmas) {
        if (turmaSelecionada == -1L && turmas.isNotEmpty()) {
            turmaSelecionada = turmas.first().id
        }
    }

    val notas by remember(turmaSelecionada) {
        vm.notasDaTurma(turmaSelecionada)
    }.collectAsState(initial = emptyList<NotaAlunoProva>())

    // ---- NOVO: frequência da turma (outra fonte, mesmo studentId) ----
    val frequencias by remember(turmaSelecionada) {
        vm.resumoFrequencia(turmaSelecionada)
    }.collectAsState(initial = emptyList<ResumoFrequencia>())

    val notasFiltradas = remember(notas, bimestreFiltro) {
        if (bimestreFiltro == 0) notas else notas.filter { it.bimester == bimestreFiltro }
    }

    val alunos = remember(notasFiltradas, frequencias, ordenacao) {
        val base = montarResumos(notasFiltradas, frequencias)
        when (ordenacao) {
            1 -> base.sortedByDescending { it.mediaGeral ?: Double.NEGATIVE_INFINITY }
            2 -> base.sortedByDescending { it.evolucao ?: Double.NEGATIVE_INFINITY }
            else -> base.sortedBy { it.nome.lowercase() }
        }
    }

    val pontosTurma = remember(notasFiltradas) { pontosDaTurma(notasFiltradas) }

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
                        titulo = "Sem dados ainda",
                        mensagem = "Lance notas em Avaliações e faça a chamada em Frequência " +
                                "para comparar os alunos."
                    )
                } else {
                    Text(
                        text = "${alunos.size} aluno(s) no comparativo",
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

                    Spacer(Modifier.height(2.dp))
                    CartaoEvolucaoTurma(pontos = pontosTurma, qtdAlunos = alunos.size)
                    CartaoFrequenciaTurma(alunos = alunos)
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

/** Card com o gráfico de linha da média da turma prova a prova. */
@Composable
private fun CartaoEvolucaoTurma(
    pontos: List<PontoGrafico>,
    qtdAlunos: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = Roxo,
                    modifier = Modifier.height(18.dp).width(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Evolução da turma",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (pontos.isEmpty()) "Sem provas lançadas"
                        else "Média dos alunos em ${pontos.size} prova(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            GraficoLinha(
                pontos = pontos,
                maxValor = 10.0,
                altura = 210.dp,
                cor = Roxo,
                mensagemVazio = "Lance notas nas avaliações para ver a evolução da turma."
            )
        }
    }
}

/** NOVO — barras de presença, do que faltou mais pro que faltou menos. */
@Composable
private fun CartaoFrequenciaTurma(alunos: List<AlunoResumo>) {
    val comChamada = alunos
        .filter { it.aulas > 0 && it.percentualPresenca != null }
        .sortedBy { it.percentualPresenca ?: 0 }

    if (comChamada.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Roxo,
                    modifier = Modifier.height(18.dp).width(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Frequência da turma",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Do que faltou mais pro que faltou menos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            GraficoBarras(
                barras = comChamada.map {
                    BarraGrafico(
                        rotulo = apelido(it.nome),
                        valor = it.percentualPresenca?.toDouble(),
                        cor = corDaPresenca(it.percentualPresenca)
                    )
                },
                maxValor = 100.0,
                altura = 210.dp,
                sufixo = "%",
                mensagemVazio = "Nenhuma chamada registrada nessa turma."
            )

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Verde ≥ 85% · Âmbar 75–84% · Vermelho < 75%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CartaoAluno(
    aluno: AlunoResumo,
    bimestreFiltro: Int,
    onClick: (AlunoResumo) -> Unit
) {
    val tendencia = tendenciaDe(aluno.evolucao)

    val textoMedia = if (aluno.notas.isEmpty()) {
        "sem notas lançadas"
    } else {
        val rotuloMedia = if (bimestreFiltro == 0) "média" else "média do ${bimestreFiltro}º bim"
        "$rotuloMedia ${formatarNumero(aluno.mediaGeral ?: 0.0)} • ${aluno.notas.size} prova(s)"
    }

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
                        text = textoMedia,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EtiquetaTendencia(tendencia)
            }

            Spacer(Modifier.height(12.dp))
            MiniGrafico(valores = aluno.porBimestre.map { it.second }, cor = tendencia.cor)

            // ---- NOVO: frequência no comparativo ----
            Spacer(Modifier.height(10.dp))
            LinhaPresenca(percentual = aluno.percentualPresenca, presentes = aluno.presentes, aulas = aluno.aulas)

            if (aluno.porBimestre.isNotEmpty()) {
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
}

/** Linha de presença com cor por faixa. Sem chamada = tom neutro. */
@Composable
private fun LinhaPresenca(percentual: Int?, presentes: Int, aulas: Int) {
    val cor = corDaPresenca(percentual)
    val texto = if (percentual == null) {
        "Sem chamada registrada"
    } else {
        "Frequência $percentual% • $presentes/$aulas aulas"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (percentual == null) Icons.Default.Remove else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.height(14.dp).width(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = cor,
            fontWeight = FontWeight.SemiBold
        )
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
                    text = if (aluno.notas.isEmpty()) "sem notas lançadas"
                    else "média geral ${formatarNumero(aluno.mediaGeral ?: 0.0)} • " +
                            "${aluno.notas.size} prova(s) lançada(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EtiquetaTendencia(tendencia)
        }

        // ---- NOVO: frequência do aluno ----
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CaixinhaNumero(
                valor = if (aluno.aulas == 0) "—" else "${aluno.presentes}",
                rotulo = "presenças",
                cor = VerdeTexto,
                modifier = Modifier.weight(1f)
            )
            CaixinhaNumero(
                valor = if (aluno.aulas == 0) "—" else "${aluno.faltas}",
                rotulo = "faltas",
                cor = VermelhoTexto,
                modifier = Modifier.weight(1f)
            )
            CaixinhaNumero(
                valor = aluno.percentualPresenca?.let { "$it%" } ?: "—",
                rotulo = "frequência",
                cor = corDaPresenca(aluno.percentualPresenca),
                modifier = Modifier.weight(1f)
            )
        }

        if (aluno.notas.isEmpty()) {
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Esse aluno ainda não tem nota lançada — só a frequência aparece aqui.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        Spacer(Modifier.height(22.dp))
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

/** Quadradinho com um número e uma legenda (presenças / faltas / frequência). */
@Composable
private fun CaixinhaNumero(
    valor: String,
    rotulo: String,
    cor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(cor.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            color = cor,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
