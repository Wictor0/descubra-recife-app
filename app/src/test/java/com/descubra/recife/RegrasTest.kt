package com.descubra.recife

import com.descubra.recife.data.demo.DadosDemo
import com.descubra.recife.domain.model.Avaliacao
import com.descubra.recife.domain.model.RoteiroRegras
import com.descubra.recife.ui.screens.auth.AuthViewModel
import com.descubra.recife.ui.screens.auth.FormCadastro
import com.descubra.recife.ui.screens.home.HomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegrasTest {
    private val marcoZero = DadosDemo.locais.first { it.id == "marco-zero" }
    private val boaViagem = DadosDemo.locais.first { it.id == "praia-boa-viagem" }

    @Test
    fun `roteiro nao duplica local ja adicionado`() {
        val lista = RoteiroRegras.comLocal(RoteiroRegras.comLocal(emptyList(), marcoZero), marcoZero)
        assertEquals(1, lista.size)
    }

    @Test
    fun `roteiro remove local`() {
        val lista = RoteiroRegras.semLocal(listOf(marcoZero, boaViagem), marcoZero.id)
        assertEquals(listOf(boaViagem), lista)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `avaliacao fora de 1 a 5 e rejeitada`() {
        Avaliacao("a", "c", "l", nota = 6, comentario = "", data = 0)
    }

    @Test
    fun `busca ignora acentos e maiusculas`() {
        val paco = DadosDemo.locais.first { it.id == "paco-do-frevo" }
        assertTrue(HomeUiState.combina(paco, "PACO"))
    }

    @Test
    fun `cadastro exige senha de 8 caracteres e confirmacao igual`() {
        val ok = FormCadastro(nomeProprio = "Ana", email = "ana@x.com", senha = "12345678", confirmar = "12345678", aceitouTermos = true)
        assertNull(AuthViewModel.validarCadastro(ok))
        assertTrue(AuthViewModel.validarCadastro(ok.copy(senha = "123", confirmar = "123"))!!.contains("8"))
        assertEquals("As senhas não conferem", AuthViewModel.validarCadastro(ok.copy(confirmar = "outra123")))
        assertTrue(AuthViewModel.validarCadastro(ok.copy(aceitouTermos = false))!!.contains("termos"))
    }

    @Test
    fun `data de nascimento formatada e validada`() {
        val f = FormCadastro(dia = "5", mes = "3", ano = "2001")
        assertEquals("05/03/2001", AuthViewModel.dataNascimento(f))
        assertNull(AuthViewModel.dataNascimento(f.copy(mes = "13")))
    }
}
