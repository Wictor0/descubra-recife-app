package com.descubra.recife.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.descubra.recife.ui.components.CardLocal

@Composable
fun HomeScreen(viewModel: HomeViewModel, onLocalClick: (String) -> Unit, onPerfilClick: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Descubra Recife",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onPerfilClick) { Text("Perfil") }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(estado.categorias, key = { it.id }) { categoria ->
                    FilterChip(
                        selected = estado.categoriaSelecionada == categoria.id,
                        onClick = { viewModel.selecionarCategoria(categoria.id) },
                        label = { Text(categoria.nome) }
                    )
                }
            }
        }
        when {
            estado.carregando -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            estado.erro != null -> item { Text(estado.erro!!, color = MaterialTheme.colorScheme.error) }
            estado.locais.isEmpty() -> item { Text("Nenhum local nesta categoria ainda.") }
            else -> items(estado.locais, key = { it.id }) { local ->
                CardLocal(local, onClick = { onLocalClick(local.id) })
            }
        }
    }
}
