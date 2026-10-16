package com.descubra.recife.ui.screens.roteiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.data.repository.RascunhoRoteiro
import com.descubra.recife.data.repository.RoteiroRepository
import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoteiroViewModel(
    private val auth: AuthRepository,
    private val roteiros: RoteiroRepository,
    private val rascunho: RascunhoRoteiro
) : ViewModel() {

    val selecionados: StateFlow<List<Local>> = rascunho.locais

    @OptIn(ExperimentalCoroutinesApi::class)
    val salvos: StateFlow<List<Roteiro>> = auth.clienteAtualId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else roteiros.roteiros(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        auth.clienteAtualId.value?.let { id -> viewModelScope.launch { roteiros.sincronizar(id) } }
    }

    fun remover(localId: String) = rascunho.remover(localId)

    fun salvar() {
        val clienteId = auth.clienteAtualId.value ?: return
        val locais = selecionados.value
        if (locais.isEmpty()) return
        viewModelScope.launch {
            roteiros.salvar(Roteiro(UUID.randomUUID().toString(), clienteId, System.currentTimeMillis(), locais))
            rascunho.limpar()
        }
    }

    fun excluir(roteiroId: String) {
        val clienteId = auth.clienteAtualId.value ?: return
        viewModelScope.launch { roteiros.excluir(clienteId, roteiroId) }
    }
}
