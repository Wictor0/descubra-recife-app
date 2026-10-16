package com.descubra.recife.ui.screens.roteiro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoteiroScreen(viewModel: RoteiroViewModel) {
    val selecionados by viewModel.selecionados.collectAsStateWithLifecycle()
    val salvos by viewModel.salvos.collectAsStateWithLifecycle()
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("Novo roteiro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (selecionados.isEmpty()) {
            item { Text("Abra um local e toque em \"Adicionar ao Roteiro\".", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(selecionados, key = { "sel-" + it.id }) { local ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(local.titulo, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.remover(local.id) }) { Text("Remover") }
            }
        }
        if (selecionados.isNotEmpty()) {
            item { Button(onClick = viewModel::salvar, modifier = Modifier.fillMaxWidth()) { Text("Salvar roteiro") } }
        }

        item {
            Text(
                "Meus roteiros",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        if (salvos.isEmpty()) {
            item { Text("Nenhum roteiro salvo. Eles ficam disponíveis mesmo sem internet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(salvos, key = { it.id }) { roteiro ->
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Roteiro de " + formato.format(Date(roteiro.dataGeracao)), fontWeight = FontWeight.Bold)
                    roteiro.locais.forEachIndexed { i, l -> Text("${i + 1}. ${l.titulo}") }
                    TextButton(onClick = { viewModel.excluir(roteiro.id) }) { Text("Excluir") }
                }
            }
        }
    }
}
