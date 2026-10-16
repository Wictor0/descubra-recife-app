package com.descubra.recife.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.descubra.recife.domain.model.Local

/** Botão "Como chegar" (ADR-004): abre a rota no app de mapas do aparelho. */
object Mapas {

    fun linkComoChegar(latitude: Double, longitude: Double): String =
        "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"

    fun abrirComoChegar(context: Context, local: Local) {
        val geo = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:${local.latitude},${local.longitude}?q=${local.latitude},${local.longitude}(${Uri.encode(local.titulo)})")
        )
        try {
            context.startActivity(geo)
        } catch (e: ActivityNotFoundException) {
            // Nenhum app de mapas instalado: cai no navegador.
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(linkComoChegar(local.latitude, local.longitude))))
        }
    }
}
