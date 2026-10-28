package com.descubra.recife.ui.screens.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.data.repository.AvaliacaoRepository
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.data.repository.RoteiroDoCliente
import com.descubra.recife.domain.model.Avaliacao
import com.descubra.recife.domain.model.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LocalUiState(
    val local: Local? = null,
    val avaliacoes: List<Avaliacao> = emptyList(),
    val carregando: Boolean = true
) {
    val media: Double? get() = avaliacoes.takeIf { it.isNotEmpty() }?.map { it.nota }?.average()
}

class LocalViewModel(
    private val localId: String,
    private val catalogo: CatalogoRepository,
    private val avaliacoes: AvaliacaoRepository,
    private val auth: AuthRepository,
    private val roteiro: RoteiroDoCliente
) : ViewModel() {

    private val _estado = MutableStateFlow(LocalUiState())
    val estado: StateFlow<LocalUiState> = _estado

    val noRoteiro: StateFlow<Boolean> = roteiro.locais
        .map { lista -> lista.any { it.id == localId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            val local = runCatching { catalogo.local(localId) }.getOrNull()
            _estado.update { it.copy(local = local, carregando = false) }
            recarregarAvaliacoes()
        }
    }

    fun alternarRoteiro() {
        val local = _estado.value.local ?: return
        viewModelScope.launch {
            if (noRoteiro.value) roteiro.remover(local.id) else roteiro.adicionar(local)
        }
    }

    fun avaliar(nota: Int, comentario: String) {
        val clienteId = auth.clienteAtualId.value ?: return
        viewModelScope.launch {
            runCatching {
                avaliacoes.avaliar(
                    Avaliacao(UUID.randomUUID().toString(), clienteId, localId, nota, comentario.trim(), System.currentTimeMillis())
                )
            }
            recarregarAvaliacoes()
        }
    }

    private suspend fun recarregarAvaliacoes() {
        val lista = runCatching { avaliacoes.avaliacoesDoLocal(localId) }.getOrDefault(emptyList())
        _estado.update { it.copy(avaliacoes = lista.sortedByDescending { a -> a.data }) }
    }
}
