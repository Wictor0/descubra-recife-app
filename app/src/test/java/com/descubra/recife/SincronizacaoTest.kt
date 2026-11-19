package com.descubra.recife

import com.descubra.recife.data.repository.Sincronizacao
import com.descubra.recife.data.repository.Sincronizacao.Versao
import org.junit.Assert.assertEquals
import org.junit.Test

/** Regra de sincronização entre a cópia do aparelho e a da nuvem (last-write-wins). */
class SincronizacaoTest {

    @Test
    fun `roteiro feito sem internet sobe para a nuvem`() {
        val plano = Sincronizacao.planejar(locais = listOf(Versao("atual", 100, pendente = true)), remotos = emptyList())
        assertEquals(setOf("atual"), plano.subir)
    }

    @Test
    fun `aparelho novo baixa o roteiro que estava na conta`() {
        val plano = Sincronizacao.planejar(locais = emptyList(), remotos = listOf(Versao("atual", 100)))
        assertEquals(setOf("atual"), plano.baixar)
        assertEquals(emptySet<String>(), plano.subir)
    }

    @Test
    fun `nuvem mais recente vence a mudanca antiga do aparelho`() {
        val plano = Sincronizacao.planejar(
            locais = listOf(Versao("atual", 100, pendente = true)),
            remotos = listOf(Versao("atual", 200))
        )
        assertEquals(setOf("atual"), plano.baixar)
        assertEquals(emptySet<String>(), plano.subir)
    }

    @Test
    fun `mudanca mais recente do aparelho vence a nuvem`() {
        val plano = Sincronizacao.planejar(
            locais = listOf(Versao("atual", 300, pendente = true)),
            remotos = listOf(Versao("atual", 200))
        )
        assertEquals(setOf("atual"), plano.subir)
        assertEquals(emptySet<String>(), plano.baixar)
    }

    @Test
    fun `roteiro ja sincronizado sempre pega a versao da nuvem`() {
        val plano = Sincronizacao.planejar(
            locais = listOf(Versao("atual", 100, pendente = false)),
            remotos = listOf(Versao("atual", 250))
        )
        assertEquals(setOf("atual"), plano.baixar)
    }

    @Test
    fun `roteiro apagado em outro aparelho sai deste tambem`() {
        val plano = Sincronizacao.planejar(locais = listOf(Versao("antigo", 100, pendente = false)), remotos = emptyList())
        assertEquals(setOf("antigo"), plano.apagarLocal)
    }
}
