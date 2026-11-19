package com.descubra.recife

import com.descubra.recife.data.demo.DadosDemo
import com.descubra.recife.data.local.RoteiroDao
import com.descubra.recife.data.local.RoteiroEntity
import com.descubra.recife.data.local.RoteiroLocalEntity
import com.descubra.recife.data.repository.RoteiroRemoto
import com.descubra.recife.data.repository.RoteiroRepositoryImpl
import com.descubra.recife.domain.model.Roteiro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Repositório do roteiro com as duas réplicas falsas: um "Room" em memória e
 * uma "nuvem" em memória que pode ficar sem internet.
 */
class RoteiroRepositoryTest {
    private val cliente = "cliente-1"
    private val marcoZero = DadosDemo.locais.first { it.id == "marco-zero" }
    private val boaViagem = DadosDemo.locais.first { it.id == "praia-boa-viagem" }

    private fun roteiro(vararg locais: com.descubra.recife.domain.model.Local, em: Long) =
        Roteiro("atual", cliente, em, locais.toList())

    @Test
    fun `sem internet o roteiro fica salvo no aparelho e sobe quando a conexao volta`() = runTest {
        val nuvem = NuvemFalsa(online = false)
        val aparelho = RoomFalso()
        val repo = RoteiroRepositoryImpl(aparelho, nuvem)

        repo.salvar(roteiro(marcoZero, em = 100))
        assertEquals(listOf("marco-zero"), repo.roteiros(cliente).first().single().locais.map { it.id })
        assertFalse(aparelho.roteiros.value.getValue("atual").sincronizado)
        assertFalse(repo.sincronizar(cliente))

        nuvem.online = true
        assertTrue(repo.sincronizar(cliente))
        assertEquals(listOf("marco-zero"), nuvem.dados.getValue("atual").locais.map { it.id })
        assertTrue(aparelho.roteiros.value.getValue("atual").sincronizado)
    }

    @Test
    fun `outro aparelho recebe o roteiro salvo na conta`() = runTest {
        val nuvem = NuvemFalsa(online = true)
        RoteiroRepositoryImpl(RoomFalso(), nuvem).salvar(roteiro(marcoZero, boaViagem, em = 100))

        // Aparelho novo, sem nada salvo (ou que perdeu o cache)
        val aparelhoNovo = RoteiroRepositoryImpl(RoomFalso(), nuvem)
        assertTrue(aparelhoNovo.roteiros(cliente).first().isEmpty())
        aparelhoNovo.sincronizar(cliente)
        assertEquals(listOf("marco-zero", "praia-boa-viagem"), aparelhoNovo.roteiros(cliente).first().single().locais.map { it.id })
    }

    @Test
    fun `ao abrir com internet o aparelho pega a versao mais nova da nuvem`() = runTest {
        val nuvem = NuvemFalsa(online = true)
        val celular = RoteiroRepositoryImpl(RoomFalso(), nuvem)
        celular.salvar(roteiro(marcoZero, em = 100))

        // Mudança feita depois em outro aparelho
        RoteiroRepositoryImpl(RoomFalso(), nuvem).salvar(roteiro(marcoZero, boaViagem, em = 200))

        celular.sincronizar(cliente)
        assertEquals(2, celular.roteiros(cliente).first().single().locais.size)
    }

    // ---- Réplicas falsas ----

    private class NuvemFalsa(var online: Boolean) : RoteiroRemoto {
        val dados = mutableMapOf<String, Roteiro>()
        private val mudancas = MutableSharedFlow<List<Roteiro>>()

        private fun conexao() { if (!online) throw IOException("sem internet") }

        override suspend fun salvar(roteiro: Roteiro) { conexao(); dados[roteiro.id] = roteiro }
        override suspend fun excluir(clienteId: String, roteiroId: String) { conexao(); dados.remove(roteiroId) }
        override suspend fun listar(clienteId: String): List<Roteiro> { conexao(); return dados.values.toList() }
        override fun observar(clienteId: String): Flow<List<Roteiro>> = mudancas
    }

    private class RoomFalso : RoteiroDao {
        val roteiros = MutableStateFlow<Map<String, RoteiroEntity>>(emptyMap())
        private val locais = MutableStateFlow<List<RoteiroLocalEntity>>(emptyList())

        override fun roteirosDoCliente(clienteId: String) =
            roteiros.map { m -> m.values.filter { it.clienteId == clienteId }.sortedByDescending { it.dataGeracao } }
        override fun locaisDoRoteiro(roteiroId: String) =
            locais.map { l -> l.filter { it.roteiroId == roteiroId }.sortedBy { it.ordem } }
        override fun locaisDosRoteiros(clienteId: String) =
            locais.map { l -> l.filter { roteiros.value[it.roteiroId]?.clienteId == clienteId }.sortedBy { it.ordem } }
        override suspend fun roteirosAgora(clienteId: String) = roteiros.value.values.filter { it.clienteId == clienteId }
        override suspend fun locaisAgora(roteiroId: String) = locais.value.filter { it.roteiroId == roteiroId }.sortedBy { it.ordem }
        override suspend fun inserirRoteiro(roteiro: RoteiroEntity) { roteiros.value = roteiros.value + (roteiro.id to roteiro) }
        override suspend fun inserirLocais(locais: List<RoteiroLocalEntity>) { this.locais.value = this.locais.value + locais }
        override suspend fun apagarLocais(roteiroId: String) { locais.value = locais.value.filterNot { it.roteiroId == roteiroId } }
        override suspend fun apagarRoteiro(roteiroId: String) { roteiros.value = roteiros.value - roteiroId; apagarLocais(roteiroId) }
        override suspend fun marcarSincronizado(roteiroId: String) {
            roteiros.value[roteiroId]?.let { roteiros.value = roteiros.value + (roteiroId to it.copy(sincronizado = true)) }
        }
    }
}
