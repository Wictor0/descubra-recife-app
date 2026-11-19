package com.descubra.recife

import androidx.compose.ui.text.AnnotatedString
import com.descubra.recife.util.DataNascimento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Campo de data do perfil: as barras aparecem sozinhas e datas impossíveis são recusadas. */
class DataNascimentoTest {

    @Test
    fun `guarda so os numeros digitados`() {
        assertEquals("10092003", DataNascimento.digitos("10/09/2003"))
        assertEquals("10092003", DataNascimento.digitos("100920031234"))
    }

    @Test
    fun `coloca as barras enquanto digita`() {
        assertEquals("10", DataNascimento.comBarras("10"))
        assertEquals("10/09", DataNascimento.comBarras("1009"))
        assertEquals("10/09/2003", DataNascimento.comBarras("10092003"))
    }

    @Test
    fun `mascara mostra as barras no campo`() {
        val mostrado = DataNascimento.mascara.filter(AnnotatedString("100920")).text.text
        assertEquals("10/09/20", mostrado)
    }

    @Test
    fun `aceita data valida e recusa data impossivel ou incompleta`() {
        assertEquals("10/09/2003", DataNascimento.valida("10092003"))
        assertEquals("29/02/2004", DataNascimento.valida("29022004"))
        assertNull(DataNascimento.valida("29022003"))
        assertNull(DataNascimento.valida("31042003"))
        assertNull(DataNascimento.valida("10132003"))
        assertNull(DataNascimento.valida("1009"))
    }
}
