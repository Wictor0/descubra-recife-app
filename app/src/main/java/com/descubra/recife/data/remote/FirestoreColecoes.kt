package com.descubra.recife.data.remote

/** Nomes das coleções do Firestore, centralizados para não espalhar strings pelo app. */
object FirestoreColecoes {
    const val CLIENTES = "clientes"
    const val CATEGORIAS = "categorias"
    const val LOCAIS = "locais"
    const val AVALIACOES = "avaliacoes"

    /** Subcoleção de [CLIENTES]: clientes/{uid}/roteiros/{id} */
    const val ROTEIROS = "roteiros"
}
