package com.example.agendaparaprofessores.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.agendaparaprofessores.data.Student
import com.example.agendaparaprofessores.ui.*
import com.example.agendaparaprofessores.ui.theme.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun StudentsScreen(
    navController: NavHostController,
    classId: Long,
    vm: AppViewModel = viewModel()
) {
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val turmas by vm.classes.collectAsState()
    val alunos by vm.alunosDaTurma(classId).collectAsState(initial = emptyList())
    val turma = turmas.firstOrNull { it.id == classId }

    var novoNome by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var nomesLidos by remember { mutableStateOf<List<String>?>(null) }
    var excluir by remember { mutableStateOf<Student?>(null) }

    val seletor = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        carregando = true
        erro = null
        escopo.launch {
            var bitmap: Bitmap? = null
            try {
                val mime = contexto.contentResolver.getType(uri)
                bitmap = withContext(Dispatchers.IO) {
                    if (mime == "application/pdf") bitmapDoPdf(contexto, uri)
                    else bitmapDaImagem(contexto, uri)
                }
                if (bitmap == null) {
                    erro = "Não consegui abrir esse arquivo."
                } else {
                    val texto = reconhecerTexto(bitmap)
                    val nomes = extrairNomes(texto)
                    if (nomes.isEmpty()) {
                        erro = "Não encontrei nenhum nome. Tente uma foto mais nítida e de frente."
                    } else {
                        nomesLidos = nomes
                    }
                }
            } catch (e: OutOfMemoryError) {
                erro = "Esse arquivo é grande demais pro celular processar."
            } catch (e: Exception) {
                erro = "Erro ao ler o arquivo: ${e.message}"
            } finally {
                bitmap?.recycle()
                carregando = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                "Alunos",
                "${turma?.name ?: "Turma"} • ${alunos.size} aluno(s)",
                onBack = { navController.popBackStack() }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = novoNome,
                    onValueChange = { novoNome = it; erro = null },
                    label = { Text("Nome do aluno") },
                    placeholder = { Text("Ex: Maria Silva") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = novoNome.isNotBlank(),
                    onClick = { vm.addAluno(classId, novoNome); novoNome = "" },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp)
                ) { Text("Add") }
            }

            OutlinedButton(
                onClick = { seletor.launch(arrayOf("image/*", "application/pdf")) },
                enabled = !carregando,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Description, null)
                Spacer(Modifier.width(10.dp))
                Text("Importar da lista (foto ou PDF)")
            }

            if (carregando) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Lendo a lista... isso leva alguns segundos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            erro?.let {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
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

            if (alunos.isEmpty() && !carregando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        Icons.Default.Person,
                        "Nenhum aluno ainda",
                        "Digite um nome acima ou importe a lista da turma por foto ou PDF.",
                        action = {
                            Button(
                                onClick = { seletor.launch(arrayOf("image/*", "application/pdf")) },
                                shape = RoundedCornerShape(16.dp)
                            ) { Text("Importar da lista") }
                        }
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(alunos, key = { it.id }) { a ->
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InitialAvatar(a.name, RoxoMedio)
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    a.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { excluir = a }) {
                                    Icon(Icons.Default.Delete, "Excluir",
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    nomesLidos?.let { nomes ->
        ConfirmarNomesDialog(
            nomes = nomes,
            onDismiss = { nomesLidos = null },
            onConfirm = { escolhidos ->
                vm.addAlunos(classId, escolhidos)
                nomesLidos = null
            }
        )
    }

    excluir?.let { a ->
        ConfirmDeleteDialog(
            titulo = "Excluir aluno?",
            mensagem = "\"${a.name}\" será removido da turma.",
            onConfirm = { vm.deleteAluno(a); excluir = null },
            onDismiss = { excluir = null }
        )
    }
}

/* ============ OCR ============ */

private suspend fun reconhecerTexto(bitmap: Bitmap): String =
    suspendCancellableCoroutine { cont ->
        val reconhecedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        reconhecedor.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { resultado ->
                val texto = resultado.textBlocks
                    .flatMap { it.lines }
                    .joinToString("\n") { it.text }
                if (cont.isActive) cont.resume(texto)
                reconhecedor.close()
            }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resumeWithException(e)
                reconhecedor.close()
            }
    }

private fun bitmapDaImagem(contexto: Context, uri: Uri): Bitmap? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val origem = ImageDecoder.createSource(contexto.contentResolver, uri)
        ImageDecoder.decodeBitmap(origem) { decoder, info, _ ->
            val l = info.size.width
            val a = info.size.height
            val maior = maxOf(l, a)
            val limite = 2400
            if (maior > limite) {
                val escala = limite.toFloat() / maior
                decoder.setTargetSize((l * escala).toInt(), (a * escala).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        @Suppress("DEPRECATION")
        android.provider.MediaStore.Images.Media.getBitmap(contexto.contentResolver, uri)
    }
} catch (e: Exception) {
    null
}

private fun bitmapDoPdf(contexto: Context, uri: Uri): Bitmap? {
    val temp = File(contexto.cacheDir, "lista_${System.currentTimeMillis()}.pdf")
    try {
        contexto.contentResolver.openInputStream(uri)?.use { entrada ->
            FileOutputStream(temp).use { saida -> entrada.copyTo(saida) }
        } ?: return null

        val fd = ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        val pagina = renderer.openPage(0)
        val escala = 2
        val bmp = Bitmap.createBitmap(
            pagina.width * escala,
            pagina.height * escala,
            Bitmap.Config.ARGB_8888
        )
        bmp.eraseColor(android.graphics.Color.WHITE)
        pagina.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        pagina.close()
        renderer.close()
        fd.close()
        return bmp
    } catch (e: Exception) {
        return null
    } finally {
        temp.delete()
    }
}

/* ============ Limpeza do texto do OCR ============ */

private val CABECALHOS = listOf(
    "nome", "nomes", "aluno", "alunos", "matrícula", "matricula",
    "nº", "n°", "no.", "lista", "presença", "presenca", "frequência",
    "frequencia", "nota", "turma", "série", "serie", "ano", "data",
    "assinatura", "escola", "professor", "professora", "disciplina"
)

private val LIGACOES = setOf("da", "de", "do", "das", "dos", "e")

private fun capitalizar(nome: String): String =
    nome.lowercase()
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { p ->
            if (p in LIGACOES) p else p.replaceFirstChar { it.uppercase() }
        }

fun extrairNomes(texto: String): List<String> =
    texto.lines()
        .map { it.replace(Regex("^\\s*\\d{1,4}\\s*[-.)/:º°]?\\s*"), "") }
        .map { it.replace(Regex("\\s+\\d{1,5}([.,]\\d+)?(\\s+\\d{1,5}([.,]\\d+)?)*\\s*$"), "") }
        .map { it.replace(Regex("[^\\p{L}\\s'.-]"), " ") }
        .map { it.replace(Regex("\\s+"), " ").trim() }
        .filter { linha ->
            linha.length >= 4 &&
                    linha.count { it.isLetter() } >= 3 &&
                    CABECALHOS.none { linha.startsWith(it, ignoreCase = true) }
        }
        .map { capitalizar(it) }
        .distinctBy { it.lowercase() }

/* ============ Diálogo de confirmação ============ */

@Composable
private fun ConfirmarNomesDialog(
    nomes: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var marcados by remember(nomes) { mutableStateOf(nomes.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Confira os nomes") },
        text = {
            Column {
                Text(
                    "Encontrei ${nomes.size} nomes. Desmarque o que não for aluno.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(nomes) { nome ->
                        val marcado = nome in marcados
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    marcados = if (marcado) marcados - nome else marcados + nome
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = marcado,
                                onCheckedChange = { c ->
                                    marcados = if (c) marcados + nome else marcados - nome
                                }
                            )
                            Text(nome, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = marcados.isNotEmpty(),
                onClick = { onConfirm(marcados.toList()) }
            ) { Text("Adicionar ${marcados.size}") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
