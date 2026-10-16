package com.descubra.recife.di

import android.content.Context
import com.descubra.recife.data.demo.AuthRepositoryDemo
import com.descubra.recife.data.demo.AvaliacaoRepositoryDemo
import com.descubra.recife.data.demo.CatalogoRepositoryDemo
import com.descubra.recife.data.local.AppDatabase
import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.data.repository.AvaliacaoRepository
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.data.repository.FirebaseAuthRepository
import com.descubra.recife.data.repository.FirestoreAvaliacaoRepository
import com.descubra.recife.data.repository.FirestoreCatalogoRepository
import com.descubra.recife.data.repository.FirestoreRoteiroRemoto
import com.descubra.recife.data.repository.RascunhoRoteiro
import com.descubra.recife.data.repository.RoteiroRepository
import com.descubra.recife.data.repository.RoteiroRepositoryImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Injeção de dependências manual: cria cada repositório uma vez só.
 * Se o Firebase não estiver configurado, usa as versões de demonstração.
 */
class AppContainer(context: Context) {

    val usandoFirebase: Boolean = FirebaseApp.getApps(context).isNotEmpty()

    private val database = AppDatabase.criar(context)

    val authRepository: AuthRepository
    val catalogoRepository: CatalogoRepository
    val avaliacaoRepository: AvaliacaoRepository
    val roteiroRepository: RoteiroRepository
    val rascunhoRoteiro = RascunhoRoteiro()

    init {
        if (usandoFirebase) {
            val db = FirebaseFirestore.getInstance()
            authRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), db)
            catalogoRepository = FirestoreCatalogoRepository(db)
            avaliacaoRepository = FirestoreAvaliacaoRepository(db)
            roteiroRepository = RoteiroRepositoryImpl(database.roteiroDao(), FirestoreRoteiroRemoto(db, catalogoRepository))
        } else {
            authRepository = AuthRepositoryDemo()
            catalogoRepository = CatalogoRepositoryDemo()
            avaliacaoRepository = AvaliacaoRepositoryDemo()
            roteiroRepository = RoteiroRepositoryImpl(database.roteiroDao(), remoto = null)
        }
    }
}
