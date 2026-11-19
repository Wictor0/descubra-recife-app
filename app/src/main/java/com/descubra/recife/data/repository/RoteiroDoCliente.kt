package com.descubra.recife.data.repository

import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import com.descubra.recife.domain.model.RoteiroRegras
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
    /** O roteiro do Cliente logado (null se ainda não existe). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val atual: Flow<Roteiro?> = auth.clienteAtualId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else roteiros.roteiros(id).map { lista -> lista.find { it.id == RoteiroRegras.ID_ROTEIRO_ATUAL } }
    }

    val locais: Flow<List<Local>> = atual.map { it?.locais.orEmpty() }

    suspend fun adicionar(local: Local) = alterar { RoteiroRegras.comLocal(it, local) }

    suspend fun remover(localId: String) = alterar { RoteiroRegras.semLocal(it, localId) }

    /** Sincroniza com a nuvem; false se estiver sem internet (ou sem login). */
    suspend fun sincronizar(): Boolean =
        auth.clienteAtualId.value?.let { roteiros.sincronizar(it) } ?: false

    /** Escuta a nuvem enquanto quem chamou estiver coletando (ex.: a aba Roteiro aberta). */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun acompanhar(): Flow<Unit> = auth.clienteAtualId.flatMapLatest { id ->
        if (id == null) emptyFlow() else roteiros.acompanhar(id)
    }

    private suspend fun alterar(mudanca: (List<Local>) -> List<Local>) {
        val clienteId = auth.clienteAtualId.value ?: return
        // Aparelho que ainda não tem o roteiro (ex.: acabou de entrar na conta):
        // busca o da nuvem antes, para não sobrescrever o que já estava lá.
        if (roteiros.roteiros(clienteId).first().none { it.id == RoteiroRegras.ID_ROTEIRO_ATUAL }) {
            roteiros.sincronizar(clienteId)
        }
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
