package com.descubra.recife.data.repository

import com.descubra.recife.domain.model.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/** Locais escolhidos pelo Cliente antes de salvar o roteiro (compartilhado entre telas). */
class RascunhoRoteiro {
    private val _locais = MutableStateFlow<List<Local>>(emptyList())
    val locais: StateFlow<List<Local>> = _locais

    fun adicionar(local: Local) = _locais.update { atual ->
        if (atual.any { it.id == local.id }) atual else atual + local
    }

    fun remover(localId: String) = _locais.update { atual -> atual.filterNot { it.id == localId } }

    fun limpar() {
        _locais.value = emptyList()
    }
}
