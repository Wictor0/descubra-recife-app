package com.descubra.recife

import com.descubra.recife.util.Mapas
import org.junit.Assert.assertEquals
import org.junit.Test

class MapasTest {
    @Test
    fun `link de como chegar aponta para as coordenadas do local`() {
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=-8.0631,-34.8711",
            Mapas.linkComoChegar(-8.0631, -34.8711)
        )
    }
}
