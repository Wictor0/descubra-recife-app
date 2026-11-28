package com.descubra.recife

import com.descubra.recife.data.demo.AvaliacaoRepositoryDemo
import com.descubra.recife.domain.model.Avaliacao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

/** Avaliações com autor e data, que só quem escreveu pode editar ou excluir. */
class AvaliacaoTest {
    private val recife = TimeZone.getTimeZone("America/Recife")
    private val dia27 = 1_795_821_000_000L // 27/11/2026 às 20:10 em Recife
    private val ana = Avaliacao("a1", "uid-ana", "bode-do-no", 4, "Muito bom", dia27, clienteNome = "Ana Clara")

    @Test
    fun `mostra quem avaliou e a data`() {
        assertEquals("Ana Clara · 27/11/2026", ana.descricao(recife))
    }

    @Test
    fun `avaliacao editada mostra a data da alteracao e mantem autor e data original`() {
        val editada = ana.editada(nota = 5, comentario = "  Excelente!  ", agora = dia27 + 3_600_000)
        assertEquals(5, editada.nota)
        assertEquals("Excelente!", editada.comentario)
        assertEquals(ana.data, editada.data)
        assertEquals("uid-ana", editada.clienteId)
        assertEquals("Ana Clara · 27/11/2026 · editada em 27/11/2026 às 21:10", editada.descricao(recife))
    }

    @Test
    fun `so o autor pode editar ou excluir`() {
        assertTrue(ana.eDe("uid-ana"))
        assertFalse(ana.eDe("uid-outra-pessoa"))
        assertFalse(ana.eDe(null))
    }

    @Test
    fun `avaliacao antiga sem nome aparece como Visitante`() {
        assertTrue(ana.copy(clienteNome = "").descricao(recife).startsWith("Visitante · "))
        assertNull(ana.editadoEm)
    }

    @Test
    fun `editar substitui a avaliacao e excluir remove`() = runTest {
        val repo = AvaliacaoRepositoryDemo()
        repo.avaliar(ana)
        repo.avaliar(ana.editada(5, "Excelente!", dia27 + 1))
        val lista = repo.avaliacoesDoLocal("bode-do-no")
        assertEquals(1, lista.size)
        assertEquals("Excelente!", lista.single().comentario)

        repo.excluir("a1")
        assertTrue(repo.avaliacoesDoLocal("bode-do-no").isEmpty())
    }
}
