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

    /** Quem está logado: as avaliações dele aparecem com "Editar" e "Excluir". */
    val clienteId: StateFlow<String?> = auth.clienteAtualId

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
            val nome = runCatching { auth.clienteAtual()?.nome }.getOrNull().orEmpty().trim()
            runCatching {
                avaliacoes.avaliar(
                    Avaliacao(
                        id = UUID.randomUUID().toString(),
                        clienteId = clienteId,
                        localId = localId,
                        nota = nota,
                        comentario = comentario.trim(),
                        data = System.currentTimeMillis(),
                        clienteNome = nome
                    )
                )
            }
            recarregarAvaliacoes()
        }
    }

    /** Salva a edição: mantém autor e data original e registra a data da alteração. */
    fun editar(original: Avaliacao, nota: Int, comentario: String) {
        if (!original.eDe(auth.clienteAtualId.value)) return
        viewModelScope.launch {
            runCatching { avaliacoes.avaliar(original.editada(nota, comentario, System.currentTimeMillis())) }
            recarregarAvaliacoes()
        }
    }

    fun excluir(avaliacao: Avaliacao) {
        if (!avaliacao.eDe(auth.clienteAtualId.value)) return
        viewModelScope.launch {
            runCatching { avaliacoes.excluir(avaliacao.id) }
            recarregarAvaliacoes()
        }
    }

    private suspend fun recarregarAvaliacoes() {
        val lista = runCatching { avaliacoes.avaliacoesDoLocal(localId) }.getOrDefault(emptyList())
        _estado.update { it.copy(avaliacoes = lista.sortedByDescending { a -> a.data }) }
    }
}
