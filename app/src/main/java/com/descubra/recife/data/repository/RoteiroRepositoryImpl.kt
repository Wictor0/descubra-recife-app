package com.descubra.recife.data.repository

import android.util.Log
import com.descubra.recife.data.local.RoteiroDao
import com.descubra.recife.data.local.RoteiroEntity
import com.descubra.recife.data.local.RoteiroLocalEntity
import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Duas réplicas do roteiro (RA-004, RA-005, ADR-003):
 * - Room, no aparelho: é o que a tela mostra, então funciona sem internet;
 * - Firestore, na nuvem: guarda o roteiro na conta, para outro aparelho ou
 *   se o app for reinstalado/perder o cache.
 * Com internet, a nuvem é consultada sempre que o roteiro é aberto e fica
 * sendo escutada enquanto ele está na tela. Ver [Sincronizacao].
 */
class RoteiroRepositoryImpl(
    private val dao: RoteiroDao,
    private val remoto: RoteiroRemoto?
) : RoteiroRepository {

    /** Evita duas sincronizações ao mesmo tempo (abrir a aba + listener). */
    private val trava = Mutex()

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
        // 1) Grava no aparelho na hora (a tela atualiza mesmo offline)
        salvarLocalmente(roteiro, sincronizado = false)
        // 2) Tenta subir para a nuvem; sem internet fica "pendente" e sobe na
        //    próxima sincronização.
        if (remoto == null) return
        subir(roteiro)
    }

    override suspend fun excluir(clienteId: String, roteiroId: String) {
        dao.apagarRoteiro(roteiroId)
        try {
            remoto?.excluir(clienteId, roteiroId)
        } catch (e: Exception) {
            Log.w(TAG, "Falha ao excluir roteiro $roteiroId no Firestore", e)
        }
    }

    override suspend fun sincronizar(clienteId: String): Boolean {
        if (remoto == null) return false
        val daNuvem = try {
            // Sem internet o Firestore pode ficar tentando por muito tempo: depois
            // de alguns segundos o app desiste e mostra a cópia do aparelho.
            withTimeout(ESPERA_SERVIDOR_MS) { remoto.listar(clienteId) }
        } catch (e: Exception) {
            Log.i(TAG, "Sem conexão: mostrando o roteiro salvo no aparelho", e)
            return false
        }
        aplicar(clienteId, daNuvem)
        return true
    }

    override fun acompanhar(clienteId: String): Flow<Unit> {
        if (remoto == null) return emptyFlow()
        return remoto.observar(clienteId)
            .map { daNuvem -> aplicar(clienteId, daNuvem) }
            .catch { e -> Log.w(TAG, "Escuta do roteiro na nuvem parou", e) }
    }

    /** Junta a réplica da nuvem com a do aparelho seguindo o plano de [Sincronizacao]. */
    private suspend fun aplicar(clienteId: String, daNuvem: List<Roteiro>) = trava.withLock {
        val noAparelho = dao.roteirosAgora(clienteId)
        val plano = Sincronizacao.planejar(
            locais = noAparelho.map { Sincronizacao.Versao(it.id, it.dataGeracao, pendente = !it.sincronizado) },
            remotos = daNuvem.map { Sincronizacao.Versao(it.id, it.dataGeracao) }
        )
        val nuvemPorId = daNuvem.associateBy { it.id }
        plano.baixar.forEach { id -> salvarLocalmente(nuvemPorId.getValue(id), sincronizado = true) }
        plano.apagarLocal.forEach { id -> dao.apagarRoteiro(id) }
        noAparelho.filter { it.id in plano.subir }.forEach { entidade ->
            subir(
                Roteiro(
                    id = entidade.id,
                    clienteId = entidade.clienteId,
                    dataGeracao = entidade.dataGeracao,
                    locais = dao.locaisAgora(entidade.id).map { it.paraLocal() }
                )
            )
        }
    }

    private suspend fun subir(roteiro: Roteiro) {
        // Offline, o Firestore guarda a escrita numa fila e só termina quando a
        // conexão volta; o tempo limite evita deixar a tela esperando.
        val enviado = try {
            withTimeoutOrNull(ESPERA_ENVIO_MS) { remoto!!.salvar(roteiro) } != null
        } catch (e: Exception) {
            Log.w(TAG, "Roteiro ${roteiro.id} ficou só no aparelho", e)
            false
        }
        if (enviado) dao.marcarSincronizado(roteiro.id)
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
        const val ESPERA_ENVIO_MS = 8_000L
        const val ESPERA_SERVIDOR_MS = 8_000L
    }
}
