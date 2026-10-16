package com.descubra.recife.ui.screens.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.data.repository.AvaliacaoRepository
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.data.repository.RascunhoRoteiro
import com.descubra.recife.domain.model.Avaliacao
import com.descubra.recife.domain.model.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LocalUiState(
    val local: Local? = null,
    val avaliacoes: List<Avaliacao> = emptyList(),
    val carregando: Boolean = true
)

class LocalViewModel(
    private val localId: String,
    private val catalogo: CatalogoRepository,
    private val avaliacoes: AvaliacaoRepository,
    private val auth: AuthRepository,
    private val rascunho: RascunhoRoteiro
) : ViewModel() {

    private val _estado = MutableStateFlow(LocalUiState())
    val estado: StateFlow<LocalUiState> = _estado

    val noRoteiro: StateFlow<List<Local>> = rascunho.locais

    init {
        viewModelScope.launch {
            val local = runCatching { catalogo.local(localId) }.getOrNull()
            _estado.update { it.copy(local = local, carregando = false) }
            recarregarAvaliacoes()
        }
    }

    fun adicionarAoRoteiro() {
        _estado.value.local?.let(rascunho::adicionar)
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
