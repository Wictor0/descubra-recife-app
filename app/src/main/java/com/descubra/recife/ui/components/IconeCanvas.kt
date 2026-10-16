package com.descubra.recife.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.descubra.recife.ui.theme.Laranja

enum class TipoIcone { VOLTAR, BUSCA, INICIO, MAPA }

/** Ícones desenhados à mão (vieram da tela de FAQ) e reaproveitados no app todo. */
@Composable
fun IconeCanvas(tipo: TipoIcone, tint: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val stroke = 2.2f * density
        val w = size.toPx()
        val h = size.toPx()
        val cX = w / 2
        when (tipo) {
            TipoIcone.VOLTAR -> {
                val backPath = Path().apply {
                    moveTo(w * 0.6f, h * 0.2f)
                    lineTo(w * 0.3f, h * 0.5f)
                    lineTo(w * 0.6f, h * 0.8f)
                }
                drawPath(backPath, color = tint, style = Stroke(width = w * 0.18f, cap = StrokeCap.Round))
            }
            TipoIcone.BUSCA -> {
                val strokeSearch = stroke * 1.2f
                drawCircle(tint, radius = w * 0.28f, center = Offset(w * 0.42f, h * 0.42f), style = Stroke(strokeSearch))
                drawLine(
                    tint,
                    start = Offset(w * 0.62f, h * 0.62f),
                    end = Offset(w * 0.88f, h * 0.88f),
                    strokeWidth = strokeSearch * 1.2f,
                    cap = StrokeCap.Round
                )
            }
            TipoIcone.INICIO -> {
                val telhadoEPassagem = Path().apply {
                    moveTo(cX, h * 0.15f)
                    lineTo(w * 0.88f, h * 0.48f)
                    lineTo(w * 0.78f, h * 0.48f)
                    lineTo(w * 0.78f, h * 0.88f)
                    lineTo(w * 0.58f, h * 0.88f)
                    lineTo(w * 0.58f, h * 0.58f)
                    lineTo(w * 0.42f, h * 0.58f)
                    lineTo(w * 0.42f, h * 0.88f)
                    lineTo(w * 0.22f, h * 0.88f)
                    lineTo(w * 0.22f, h * 0.48f)
                    lineTo(w * 0.12f, h * 0.48f)
                    close()
                }
                drawPath(telhadoEPassagem, color = tint)
            }
            TipoIcone.MAPA -> {
                val strokeMap = stroke * 0.9f
                val pinPath = Path().apply {
                    moveTo(w * 0.30f, h * 0.08f)
                    cubicTo(w * 0.20f, h * 0.08f, w * 0.20f, h * 0.28f, w * 0.30f, h * 0.40f)
                    cubicTo(w * 0.40f, h * 0.28f, w * 0.40f, h * 0.08f, w * 0.30f, h * 0.08f)
                    close()
                }
                drawPath(pinPath, color = tint)
                drawCircle(Laranja, radius = w * 0.05f, center = Offset(w * 0.30f, h * 0.20f))

                val mapa = Path().apply {
                    moveTo(w * 0.18f, h * 0.48f)
                    lineTo(w * 0.42f, h * 0.38f)
                    lineTo(w * 0.42f, h * 0.82f)
                    lineTo(w * 0.18f, h * 0.92f)
                    close()

                    moveTo(w * 0.42f, h * 0.38f)
                    lineTo(w * 0.66f, h * 0.48f)
                    lineTo(w * 0.66f, h * 0.92f)
                    lineTo(w * 0.42f, h * 0.82f)
                    close()

                    moveTo(w * 0.66f, h * 0.48f)
                    lineTo(w * 0.88f, h * 0.38f)
                    lineTo(w * 0.88f, h * 0.82f)
                    lineTo(w * 0.66f, h * 0.92f)
                    close()
                }
                drawPath(mapa, color = tint, style = Stroke(strokeMap, cap = StrokeCap.Round))
            }
        }
    }
}
