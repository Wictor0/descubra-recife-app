package com.descubra.recife.util

import androidx.annotation.DrawableRes
import com.descubra.recife.R

/**
 * Imagens que vieram do protótipo e ficam dentro do app (funcionam offline).
 * Locais sem foto aqui mostram a textura do banner como fundo padrão.
 */
object Imagens {
    private val locais = mapOf(
        "marco-zero" to R.drawable.local_marco_zero,
        "instituto-ricardo-brennand" to R.drawable.local_instituto_ricardo_brennand,
        "rua-do-bom-jesus" to R.drawable.local_rua_do_bom_jesus,
        "praia-boa-viagem" to R.drawable.local_praia_boa_viagem,
        "embaixada-dos-bonecos-gigantes" to R.drawable.local_embaixada_dos_bonecos_gigantes,
        "bode-do-no" to R.drawable.local_bode_do_no,
        "centro-de-artesanato" to R.drawable.local_centro_de_artesanato,
        "paco-do-frevo" to R.drawable.local_paco_do_frevo
    )

    private val categorias = mapOf(
        "praias" to R.drawable.categoria_praias,
        "gastronomia" to R.drawable.categoria_gastronomia,
        "cultura" to R.drawable.categoria_cultura,
        "folia" to R.drawable.categoria_folia,
        "historia" to R.drawable.categoria_historia
    )

    @DrawableRes
    fun doLocal(localId: String): Int? = locais[localId]

    @DrawableRes
    fun daCategoria(slug: String): Int? = categorias[slug]
}
