package com.descubra.recife.data.repository

import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import com.descubra.recife.domain.model.RoteiroRegras
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * "Seu Roteiro" do protótipo: um roteiro por Cliente, que vai sendo montado
 * ao tocar em "Adicionar ao roteiro". Cada mudança já fica salva (Room + Firestore).
 */
class RoteiroDoCliente(
    private val auth: AuthRepository,
    private val roteiros: RoteiroRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val locais: Flow<List<Local>> = auth.clienteAtualId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else roteiros.roteiros(id).map { lista ->
            lista.find { it.id == RoteiroRegras.ID_ROTEIRO_ATUAL }?.locais.orEmpty()
        }
    }

    suspend fun adicionar(local: Local) = alterar { RoteiroRegras.comLocal(it, local) }

    suspend fun remover(localId: String) = alterar { RoteiroRegras.semLocal(it, localId) }

    suspend fun sincronizar() {
        auth.clienteAtualId.value?.let { roteiros.sincronizar(it) }
    }

    private suspend fun alterar(mudanca: (List<Local>) -> List<Local>) {
        val clienteId = auth.clienteAtualId.value ?: return
        roteiros.salvar(
            Roteiro(
                id = RoteiroRegras.ID_ROTEIRO_ATUAL,
                clienteId = clienteId,
                dataGeracao = System.currentTimeMillis(),
                locais = mudanca(locais.first())
            )
        )
    }
}
