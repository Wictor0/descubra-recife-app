package com.descubra.recife

import com.descubra.recife.data.demo.DadosDemo
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import com.descubra.recife.ui.screens.home.HomeViewModel
import com.descubra.recife.ui.screens.roteiro.FiltrosRoteiro
import com.descubra.recife.ui.screens.roteiro.RoteiroUiState
import com.descubra.recife.ui.screens.roteiro.Sincronia
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** ViewModel da tela inicial e filtros do roteiro, com um catálogo falso. */
@OptIn(ExperimentalCoroutinesApi::class)
class TelasTest {

    @Before
    fun usarDispatcherDeTeste() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun restaurar() = Dispatchers.resetMain()

    private class CatalogoFalso(private val falhar: Boolean = false) : CatalogoRepository {
        override suspend fun categorias(): List<Categoria> = if (falhar) throw IOException("sem internet") else DadosDemo.categorias
        override suspend fun locais(categoriaId: String?): List<Local> = if (falhar) throw IOException("sem internet") else DadosDemo.locais
        override suspend fun local(id: String) = DadosDemo.locais.find { it.id == id }
    }

    @Test
    fun `tela inicial carrega categorias e locais`() {
        val estado = HomeViewModel(CatalogoFalso()).estado.value
        assertFalse(estado.carregando)
        assertEquals(DadosDemo.categorias.size, estado.categorias.size)
        assertNull(estado.resultados)
    }

    @Test
    fun `filtrar por categoria mostra so os locais dela e tocar de novo limpa o filtro`() {
        val vm = HomeViewModel(CatalogoFalso())
        vm.selecionarCategoria("praias")
        val resultado = vm.estado.value.resultados!!
        assertTrue(resultado.isNotEmpty())
        assertTrue(resultado.all { "praias" in it.categoriaIds })

        vm.selecionarCategoria("praias")
        assertNull(vm.estado.value.resultados)
    }

    @Test
    fun `busca encontra o local pelo nome sem acento`() {
        val vm = HomeViewModel(CatalogoFalso())
        vm.buscar("paco")
        assertEquals(listOf("paco-do-frevo"), vm.estado.value.resultados!!.map { it.id })
    }

    @Test
    fun `sem internet a tela inicial mostra erro e permite tentar de novo`() {
        val estado = HomeViewModel(CatalogoFalso(falhar = true)).estado.value
        assertFalse(estado.carregando)
        assertTrue(estado.erro!!.contains("sem internet"))
    }

    @Test
    fun `roteiro filtra por nome e ordena de A a Z`() {
        val locais = DadosDemo.locais
        val porLocal = locais.associate { it.id to it.categoriaIds }

        val busca = FiltrosRoteiro(busca = "frevo").aplicar(locais, porLocal)
        assertEquals(listOf("paco-do-frevo"), busca.map { it.id })

        val ordenado = FiltrosRoteiro(alfabetica = true).aplicar(locais, porLocal).map { it.titulo }
        assertEquals(ordenado.sorted(), ordenado)
    }

    @Test
    fun `roteiro mostra a data e hora da versao que esta na tela`() {
        val recife = TimeZone.getTimeZone("America/Recife")
        val alteradoEm = 1795098600000L // 19/11/2026 às 11:30 em Recife
        val online = RoteiroUiState(sincronia = Sincronia.NA_NUVEM, alteradoEm = alteradoEm)
        assertEquals("Atualizado em 19/11/2026 às 11:30", online.textoVersao(recife))
        val offline = online.copy(sincronia = Sincronia.SO_NO_APARELHO)
        assertEquals("Sem internet · versão de 19/11/2026 às 11:30", offline.textoVersao(recife))
        assertEquals("Sincronizando com a nuvem...", online.copy(sincronia = Sincronia.SINCRONIZANDO).textoVersao(recife))
    }
}
