package com.descubra.recife.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de data "dd/mm/aaaa": o usuário digita só os números e as barras
 * aparecem sozinhas. As regras ficam aqui para serem testadas sem Android.
 */
object DataNascimento {
    private const val DIGITOS = 8

    /** Mantém só os números digitados (no máximo 8: ddmmaaaa). */
    fun digitos(entrada: String): String = entrada.filter(Char::isDigit).take(DIGITOS)

    /** "10092003" -> "10/09/2003"; incompleto: "1009" -> "10/09". */
    fun comBarras(digitos: String): String = buildString {
        digitos.forEachIndexed { i, c ->
            if (i == 2 || i == 4) append('/')
            append(c)
        }
    }

    /** Data completa e possível ("dd/mm/aaaa"), ou null. */
    fun valida(digitos: String, anoAtual: Int = 2026): String? {
        if (digitos.length != DIGITOS) return null
        val dia = digitos.substring(0, 2).toInt()
        val mes = digitos.substring(2, 4).toInt()
        val ano = digitos.substring(4, 8).toInt()
        if (mes !in 1..12 || ano !in 1900..anoAtual) return null
        val diasNoMes = when (mes) {
            2 -> if (ano % 4 == 0 && (ano % 100 != 0 || ano % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        return if (dia in 1..diasNoMes) comBarras(digitos) else null
    }

    /** Mostra as barras no campo sem colocá-las no texto digitado. */
    val mascara = VisualTransformation { texto ->
        val formatado = comBarras(texto.text)
        TransformedText(AnnotatedString(formatado), object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset + when {
                offset > 4 -> 2
                offset > 2 -> 1
                else -> 0
            }

            override fun transformedToOriginal(offset: Int) = when {
                offset > 5 -> offset - 2
                offset > 2 -> offset - 1
                else -> offset
            }.coerceIn(0, texto.text.length)
        })
    }
}
