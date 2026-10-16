package com.descubra.recife.ui.screens.busca

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.domain.model.Local
import com.descubra.recife.ui.components.CardLocal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BuscaViewModel(catalogo: CatalogoRepository) : ViewModel() {
    private val todos = MutableStateFlow<List<Local>>(emptyList())
    val termo = MutableStateFlow("")

    val resultados: StateFlow<List<Local>> = combine(todos, termo) { locais, t ->
        if (t.isBlank()) locais
        else locais.filter { it.titulo.contains(t, ignoreCase = true) || it.endereco.contains(t, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { runCatching { catalogo.locais() }.onSuccess { todos.value = it } }
    }
}

@Composable
fun BuscaScreen(viewModel: BuscaViewModel, onLocalClick: (String) -> Unit) {
    val termo by viewModel.termo.collectAsStateWithLifecycle()
    val resultados by viewModel.resultados.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = termo,
                onValueChange = { viewModel.termo.value = it },
                label = { Text("Pesquise aqui...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(resultados, key = { it.id }) { local -> CardLocal(local) { onLocalClick(local.id) } }
    }
}
