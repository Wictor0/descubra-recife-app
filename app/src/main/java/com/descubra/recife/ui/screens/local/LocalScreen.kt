package com.descubra.recife.ui.screens.local

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.descubra.recife.ui.components.IconeCanvas
import com.descubra.recife.ui.components.TipoIcone
import com.descubra.recife.util.Mapas

@Composable
fun LocalScreen(viewModel: LocalViewModel, onVoltar: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val noRoteiro by viewModel.noRoteiro.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var nota by rememberSaveable { mutableIntStateOf(5) }
    var comentario by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.clickable(onClick = onVoltar).padding(4.dp)) {
            IconeCanvas(TipoIcone.VOLTAR, MaterialTheme.colorScheme.primary, 20.dp)
        }
        val local = estado.local
        if (local == null) {
            Text(if (estado.carregando) "Carregando..." else "Local não encontrado.")
            return@Column
        }

        Text(local.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(local.descricao)
        Info("Endereço", local.endereco)
        Info("Custo de acesso", local.custoAcesso)
        Info("Horário", local.horarioFuncionamento)
        Info("Contato", local.contato)

        Button(onClick = { Mapas.abrirComoChegar(context, local) }, modifier = Modifier.fillMaxWidth()) {
            Text("Como chegar")
        }
        val jaAdicionado = noRoteiro.any { it.id == local.id }
        OutlinedButton(
            onClick = viewModel::adicionarAoRoteiro,
            enabled = !jaAdicionado,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (jaAdicionado) "Já está no roteiro" else "Adicionar ao Roteiro") }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("Avaliações", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Row {
            (1..5).forEach { n ->
                TextButton(onClick = { nota = n }) { Text(if (n <= nota) "★" else "☆", fontSize = 22.sp) }
            }
        }
        OutlinedTextField(comentario, { comentario = it }, label = { Text("Comentário") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { viewModel.avaliar(nota, comentario); comentario = "" }) { Text("Enviar avaliação") }

        if (estado.avaliacoes.isEmpty()) Text("Seja o primeiro a avaliar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        estado.avaliacoes.forEach { a ->
            Column {
                Text("★".repeat(a.nota) + "☆".repeat(5 - a.nota))
                if (a.comentario.isNotBlank()) Text(a.comentario, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Info(rotulo: String, valor: String) {
    if (valor.isBlank()) return
    Column {
        Text(rotulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor)
    }
}
