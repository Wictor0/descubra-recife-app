package com.descubra.recife

import com.descubra.recife.data.demo.DadosDemo
import com.descubra.recife.data.repository.RascunhoRoteiro
import com.descubra.recife.domain.model.Avaliacao
import org.junit.Assert.assertEquals
import org.junit.Test

class RascunhoRoteiroTest {
    private val marcoZero = DadosDemo.locais.first { it.id == "marco-zero" }
    private val boaViagem = DadosDemo.locais.first { it.id == "boa-viagem" }

    @Test
    fun `nao duplica local ja adicionado`() {
        val rascunho = RascunhoRoteiro()
        rascunho.adicionar(marcoZero)
        rascunho.adicionar(marcoZero)
        assertEquals(1, rascunho.locais.value.size)
    }

    @Test
    fun `remove e limpa locais`() {
        val rascunho = RascunhoRoteiro()
        rascunho.adicionar(marcoZero)
        rascunho.adicionar(boaViagem)
        rascunho.remover(marcoZero.id)
        assertEquals(listOf(boaViagem), rascunho.locais.value)
        rascunho.limpar()
        assertEquals(emptyList<Any>(), rascunho.locais.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `avaliacao fora de 1 a 5 e rejeitada`() {
        Avaliacao("a", "c", "l", nota = 6, comentario = "", data = 0)
    }
}
