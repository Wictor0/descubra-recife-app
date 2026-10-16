package com.descubra.recife.data.repository

import android.util.Log
import com.descubra.recife.data.local.RoteiroDao
import com.descubra.recife.data.local.RoteiroEntity
import com.descubra.recife.data.local.RoteiroLocalEntity
import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Room é a fonte da verdade para a tela (funciona offline, RA-004);
 * o Firestore é espelho para sincronizar entre aparelhos (RA-005, ADR-003).
 */
class RoteiroRepositoryImpl(
    private val dao: RoteiroDao,
    private val remoto: RoteiroRemoto?
) : RoteiroRepository {

    override fun roteiros(clienteId: String): Flow<List<Roteiro>> =
        combine(dao.roteirosDoCliente(clienteId), dao.locaisDosRoteiros(clienteId)) { roteiros, locais ->
            val porRoteiro = locais.groupBy { it.roteiroId }
            roteiros.map { r ->
                Roteiro(
                    id = r.id,
                    clienteId = r.clienteId,
                    dataGeracao = r.dataGeracao,
                    locais = porRoteiro[r.id].orEmpty().map { it.paraLocal() }
                )
            }
        }

    override suspend fun salvar(roteiro: Roteiro) {
        salvarLocalmente(roteiro, sincronizado = false)
        if (remoto == null) return
        try {
            remoto.salvar(roteiro)
            dao.marcarSincronizado(roteiro.id)
        } catch (e: Exception) {
            // Sem rede: fica salvo no aparelho e sobe na próxima sincronização.
            Log.w(TAG, "Roteiro ${roteiro.id} salvo só localmente", e)
        }
    }

    override suspend fun excluir(clienteId: String, roteiroId: String) {
        dao.apagarRoteiro(roteiroId)
        try {
            remoto?.excluir(clienteId, roteiroId)
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao excluir roteiro $roteiroId no Firestore", e)
        }
    }

    override suspend fun sincronizar(clienteId: String) {
        if (remoto == null) return
        try {
            remoto.listar(clienteId).forEach { salvarLocalmente(it, sincronizado = true) }
        } catch (e: Exception) {
            Log.w(TAG, "Sincronização de roteiros falhou", e)
        }
    }

    private suspend fun salvarLocalmente(roteiro: Roteiro, sincronizado: Boolean) {
        dao.salvar(
            RoteiroEntity(roteiro.id, roteiro.clienteId, roteiro.dataGeracao, sincronizado),
            roteiro.locais.mapIndexed { i, local ->
                RoteiroLocalEntity(
                    roteiroId = roteiro.id,
                    localId = local.id,
                    ordem = i,
                    titulo = local.titulo,
                    endereco = local.endereco,
                    latitude = local.latitude,
                    longitude = local.longitude
                )
            }
        )
    }

    private fun RoteiroLocalEntity.paraLocal() = Local(
        id = localId,
        titulo = titulo,
        descricao = "",
        endereco = endereco,
        custoAcesso = "",
        horarioFuncionamento = "",
        contato = "",
        latitude = latitude,
        longitude = longitude
    )

    private companion object {
        const val TAG = "RoteiroRepository"
    }
}
