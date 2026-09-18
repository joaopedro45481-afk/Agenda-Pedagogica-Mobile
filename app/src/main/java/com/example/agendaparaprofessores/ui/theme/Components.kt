package com.example.agendaparaprofessores.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.agendaparaprofessores.data.LessonReport
import com.example.agendaparaprofessores.ui.theme.*
import kotlin.math.roundToInt

fun formatarData(r: LessonReport) = "%02d/%02d/%04d".format(r.day, r.month, r.year)

@Composable
fun coresDificuldade(dificuldade: String): Pair<Color, Color> = when {
    dificuldade.startsWith("Fácil", true) || dificuldade.startsWith("Facil", true) -> VerdeFundo to VerdeTexto
    dificuldade.startsWith("Difícil", true) || dificuldade.startsWith("Dificil", true) -> VermelhoFundo to VermelhoTexto
    else -> AmbarFundo to AmbarTexto
}

/** Cabeçalho roxo com gradiente (Home). */
@Composable
fun GradientHeader(content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(RoxoEscuro, Roxo, RoxoMedio)))
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 30.dp),
            content = content
        )
    }
}

@Composable
fun StatPill(valor: String, legenda: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(valor, style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(legenda, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
    }
}

@Composable
fun InitialAvatar(texto: String, cor: Color = Roxo) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(cor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto.trim().take(1).uppercase().ifEmpty { "?" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = cor
        )
    }
}

@Composable
fun DifficultyChip(texto: String) {
    val (fundo, tinta) = coresDificuldade(texto)
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(fundo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.labelMedium, color = tinta, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SelectableChip(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    val fundo = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val tinta = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(fundo)
            .border(
                1.dp,
                if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge, color = tinta)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    subtitulo: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(titulo, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitulo?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun SectionCard(titulo: String, icone: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(icone, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(10.dp))
                Text(titulo, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun HomeCard(icone: ImageVector, titulo: String, subtitulo: String, cor: Color, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(cor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) { Icon(icone, null, Modifier.size(26.dp), tint = cor) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(subtitulo, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ReportCard(
    report: LessonReport,
    nomeMateria: String,
    nomeTurma: String,
    onClick: () -> Unit
) {
    val (_, tinta) = coresDificuldade(report.difficulty)
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(tinta.copy(alpha = 0.7f)))
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(report.title, style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(8.dp))
                    DifficultyChip(report.difficulty)
                }
                Spacer(Modifier.height(6.dp))
                Text("$nomeTurma • $nomeMateria", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (report.summary.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(report.summary, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(formatarData(report), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(14.dp))
                    Text("${report.bimester}º Bimestre", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icone: ImageVector,
    titulo: String,
    mensagem: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Icon(icone, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(18.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(mensagem, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (action != null) { Spacer(Modifier.height(18.dp)); action() }
    }
}

@Composable
fun ConfirmDeleteDialog(
    titulo: String,
    mensagem: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(titulo) },
        text = { Text(mensagem) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Excluir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/** Campo de seleção com visual de TextField e menu suspenso (sem API experimental). */
@Composable
fun DropdownField(
    label: String,
    itens: List<Pair<Long, String>>,
    selecionadoId: Long?,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSelect: (Long) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    val texto = itens.firstOrNull { it.first == selecionadoId }?.second.orEmpty()
    Box(modifier) {
        OutlinedTextField(
            value = texto,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            shape = RoundedCornerShape(16.dp),
            trailingIcon = { Icon(Icons.Default.ExpandMore, null) },
            modifier = Modifier.fillMaxWidth()
        )
        if (!aberto) Box(Modifier.matchParentSize().clickable { aberto = true })
        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (itens.isEmpty()) {
                DropdownMenuItem(text = { Text("Nada cadastrado ainda") }, onClick = { aberto = false })
            } else {
                itens.forEach { (id, nome) ->
                    DropdownMenuItem(text = { Text(nome) }, onClick = { onSelect(id); aberto = false })
                }
            }
        }
    }
}

data class PontoGrafico(
    val rotulo: String,
    val valor: Double
)

@Composable
fun GraficoLinha(
    pontos: List<PontoGrafico>,
    maxValor: Double = 10.0,
    altura: Dp = 210.dp,
    cor: Color = MaterialTheme.colorScheme.primary,
    mensagemVazio: String = "Nenhum dado disponível"
) {
    if (pontos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(altura),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mensagemVazio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        return
    }

    val corGrade = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val corPonto = MaterialTheme.colorScheme.surface

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(altura - 30.dp)
        ) {
            val margemHorizontal = 32f
            val margemVertical = 16f
            val larguraUtil = size.width - margemHorizontal * 2
            val alturaUtil = size.height - margemVertical * 2

            fun pontoDe(i: Int): androidx.compose.ui.geometry.Offset {
                val x = if (pontos.size <= 1) {
                    size.width / 2f
                } else {
                    margemHorizontal + i * larguraUtil / (pontos.size - 1).toFloat()
                }
                val normalizado = (pontos[i].valor / maxValor).coerceIn(0.0, 1.0)
                val y = size.height - margemVertical - (normalizado * alturaUtil).toFloat()
                return androidx.compose.ui.geometry.Offset(x, y)
            }

            for (i in 0..4) {
                val y = margemVertical + i * alturaUtil / 4f
                drawLine(
                    color = corGrade,
                    start = androidx.compose.ui.geometry.Offset(margemHorizontal, y),
                    end = androidx.compose.ui.geometry.Offset(size.width - margemHorizontal, y),
                    strokeWidth = 1f
                )
            }

            if (pontos.size > 1) {
                val caminho = Path()
                val primeiro = pontoDe(0)
                caminho.moveTo(primeiro.x, primeiro.y)
                for (i in 1 until pontos.size) {
                    val atual = pontoDe(i)
                    caminho.lineTo(atual.x, atual.y)
                }
                drawPath(caminho, cor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f, cap = StrokeCap.Round))
            }

            for (i in pontos.indices) {
                val p = pontoDe(i)
                drawCircle(color = corPonto, radius = 8f, center = p)
                drawCircle(color = cor, radius = 5f, center = p)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            pontos.forEach { ponto ->
                Text(
                    text = ponto.rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class BarraGrafico(
    val rotulo: String,
    val valor: Double?,
    val cor: Color
)

@Composable
fun GraficoBarras(
    barras: List<BarraGrafico>,
    maxValor: Double = 100.0,
    altura: Dp = 210.dp,
    sufixo: String = "",
    mensagemVazio: String = "Nenhum dado disponível"
) {
    if (barras.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(altura),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mensagemVazio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        return
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(altura)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            barras.forEach { barra ->
                val valor = barra.valor ?: 0.0
                val proporcao = (valor / maxValor).coerceIn(0.0, 1.0)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "${valor.roundToInt()}$sufixo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = barra.cor
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(proporcao.toFloat().coerceIn(0.01f, 1.0f))
                                .background(barra.cor.copy(alpha = 0.85f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = barra.rotulo,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


