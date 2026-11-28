package com.descubra.recife.ui.screens.local

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import com.descubra.recife.domain.model.Avaliacao
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.descubra.recife.ui.components.BotaoPilula
import com.descubra.recife.ui.components.BotaoVoltarCirculo
import com.descubra.recife.ui.components.FolhaConteudo
import com.descubra.recife.ui.components.FotoLocal
import com.descubra.recife.ui.theme.Laranja
import com.descubra.recife.ui.theme.Poppins
import com.descubra.recife.util.Mapas

/** Detalhe do local: não existe no protótipo, então segue o estilo das outras telas. */
@Composable
fun LocalScreen(viewModel: LocalViewModel, onVoltar: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val noRoteiro by viewModel.noRoteiro.collectAsStateWithLifecycle()
    val clienteId by viewModel.clienteId.collectAsStateWithLifecycle()
    var nota by rememberSaveable { mutableIntStateOf(5) }
    var comentario by rememberSaveable { mutableStateOf("") }
    /** id da avaliação que está sendo editada no formulário (null = nova avaliação). */
    var editandoId by rememberSaveable { mutableStateOf<String?>(null) }
    var paraExcluir by remember { mutableStateOf<Avaliacao?>(null) }

    paraExcluir?.let { alvo ->
        AlertDialog(
            onDismissRequest = { paraExcluir = null },
            title = { Text("Excluir avaliação?", fontFamily = Poppins) },
            text = { Text("Sua avaliação deste local será apagada. Isso não pode ser desfeito.", fontFamily = Poppins) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.excluir(alvo)
                    if (editandoId == alvo.id) { editandoId = null; comentario = ""; nota = 5 }
                    paraExcluir = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error, fontFamily = Poppins) }
            },
            dismissButton = { TextButton(onClick = { paraExcluir = null }) { Text("Cancelar", fontFamily = Poppins) } }
        )
    }
    val context = LocalContext.current
    val local = estado.local

    if (local == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (estado.carregando) CircularProgressIndicator()
            else Text("Local não encontrado.", fontFamily = Poppins)
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            FotoLocal(local, Modifier.fillMaxSize())
            BotaoVoltarCirculo(onVoltar, Modifier.statusBarsPadding().padding(16.dp))
        }
        FolhaConteudo {
            Column(
                Modifier.padding(horizontal = 28.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    local.titulo,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                estado.media?.let { media ->
                    Text(
                        "★ %.1f · %d Reviews".format(media, estado.avaliacoes.size),
                        fontFamily = Poppins,
                        color = Laranja
                    )
                }
                Text(local.descricao, fontFamily = Poppins, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)

                Info(Icons.Filled.Place, local.endereco)
                Info(Icons.Outlined.Schedule, local.horarioFuncionamento)
                Info(Icons.Outlined.Payments, local.custoAcesso)
                Info(Icons.Outlined.Phone, local.contato)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 6.dp)) {
                    BotaoPilula(
                        "COMO CHEGAR",
                        onClick = { Mapas.abrirComoChegar(context, local) },
                        negrito = true,
                        fontSize = 15,
                        modifier = Modifier.fillMaxWidth()
                    )
                    local.cardapio?.let { url ->
                        BotaoPilula(
                            "VER CARDÁPIO",
                            onClick = { Mapas.abrirLink(context, url) },
                            negrito = true,
                            fontSize = 15,
                            cor = MaterialTheme.colorScheme.surface,
                            corTexto = Laranja,
                            modifier = Modifier.fillMaxWidth().border(2.dp, Laranja, RoundedCornerShape(50))
                        )
                    }
                    BotaoPilula(
                        if (noRoteiro) "Remover do roteiro" else "Adicionar ao Roteiro",
                        onClick = viewModel::alternarRoteiro,
                        cor = if (noRoteiro) MaterialTheme.colorScheme.secondaryContainer else Laranja,
                        corTexto = if (noRoteiro) MaterialTheme.colorScheme.onSurface else Color.White,
                        fontSize = 15,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Avaliações", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                val emEdicao = estado.avaliacoes.find { it.id == editandoId }
                if (emEdicao != null) {
                    Text("Editando sua avaliação", fontFamily = Poppins, fontSize = 13.sp, color = Laranja)
                }
                Row {
                    (1..5).forEach { n ->
                        Text(
                            if (n <= nota) "★" else "☆",
                            fontSize = 30.sp,
                            color = Laranja,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { nota = n }
                        )
                    }
                }
                OutlinedTextField(
                    comentario,
                    { comentario = it },
                    label = { Text("Conte como foi sua visita", fontFamily = Poppins) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Laranja, focusedLabelColor = Laranja),
                    modifier = Modifier.fillMaxWidth()
                )
                if (emEdicao != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BotaoPilula("Salvar alteração", onClick = {
                            viewModel.editar(emEdicao, nota, comentario)
                            editandoId = null; comentario = ""; nota = 5
                        })
                        BotaoPilula(
                            "Cancelar",
                            onClick = { editandoId = null; comentario = ""; nota = 5 },
                            cor = MaterialTheme.colorScheme.secondaryContainer,
                            corTexto = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    BotaoPilula("Enviar avaliação", onClick = { viewModel.avaliar(nota, comentario); comentario = "" })
                }

                if (estado.avaliacoes.isEmpty()) {
                    Text("Seja o primeiro a avaliar.", fontFamily = Poppins, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                estado.avaliacoes.forEach { a ->
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        a.clienteNome.ifBlank { "Visitante" },
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        a.descricao().substringAfter(" · "),
                                        fontFamily = Poppins,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // Só aparecem na avaliação de quem está logado
                                if (a.eDe(clienteId)) {
                                    IconeDiscreto(Icons.Outlined.Edit, "Editar avaliação") {
                                        editandoId = a.id; nota = a.nota; comentario = a.comentario
                                    }
                                    IconeDiscreto(Icons.Outlined.Delete, "Excluir avaliação") { paraExcluir = a }
                                }
                            }
                            Text("★".repeat(a.nota) + "☆".repeat(5 - a.nota), color = Laranja, modifier = Modifier.padding(top = 4.dp))
                            if (a.comentario.isNotBlank()) {
                                Text(a.comentario, fontFamily = Poppins, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Ícone pequeno e cinza (editar/excluir), para não competir com o texto. */
@Composable
private fun IconeDiscreto(icone: ImageVector, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, contentDescription = descricao, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun Info(icone: ImageVector, valor: String) {
    if (valor.isBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = Laranja, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(valor, fontFamily = Poppins, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
