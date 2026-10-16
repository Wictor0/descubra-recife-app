package com.descubra.recife.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val categorias: List<Categoria> = emptyList(),
    val categoriaSelecionada: String? = null,
    val locais: List<Local> = emptyList(),
    val carregando: Boolean = true,
    val erro: String? = null
)

class HomeViewModel(private val catalogo: CatalogoRepository) : ViewModel() {

    private val _estado = MutableStateFlow(HomeUiState())
    val estado: StateFlow<HomeUiState> = _estado

    init {
        viewModelScope.launch {
            runCatching { catalogo.categorias() }
                .onSuccess { categorias -> _estado.update { it.copy(categorias = categorias) } }
            carregarLocais(null)
        }
    }

    fun selecionarCategoria(id: String?) {
        val nova = if (_estado.value.categoriaSelecionada == id) null else id
        viewModelScope.launch { carregarLocais(nova) }
    }

    private suspend fun carregarLocais(categoriaId: String?) {
        _estado.update { it.copy(carregando = true, categoriaSelecionada = categoriaId, erro = null) }
        runCatching { catalogo.locais(categoriaId) }
            .onSuccess { locais -> _estado.update { it.copy(locais = locais, carregando = false) } }
            .onFailure { e -> _estado.update { it.copy(carregando = false, erro = e.message ?: "Erro ao carregar locais") } }
    }
}
