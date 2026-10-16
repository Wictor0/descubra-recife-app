package com.descubra.recife.data.repository

import com.descubra.recife.data.remote.FirestoreColecoes
import com.descubra.recife.domain.model.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

/** Credenciais ficam no Firebase Authentication; o perfil, em clientes/{uid}. */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    private val _clienteAtualId = MutableStateFlow(auth.currentUser?.uid)
    override val clienteAtualId: StateFlow<String?> = _clienteAtualId

    init {
        auth.addAuthStateListener { _clienteAtualId.value = it.currentUser?.uid }
    }

    override suspend fun entrar(email: String, senha: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, senha).await()
        Unit
    }

    override suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        dataNascimento: String?,
        telefone: String?
    ): Result<Unit> = runCatching {
        val uid = auth.createUserWithEmailAndPassword(email, senha).await().user!!.uid
        db.collection(FirestoreColecoes.CLIENTES).document(uid).set(
            mapOf(
                "nome" to nome,
                "email" to email,
                "dataNascimento" to dataNascimento,
                "telefone" to telefone
            )
        ).await()
        Unit
    }

    override suspend fun clienteAtual(): Cliente? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection(FirestoreColecoes.CLIENTES).document(uid).get().await()
        return Cliente(
            id = uid,
            nome = doc.getString("nome").orEmpty(),
            email = doc.getString("email") ?: auth.currentUser?.email.orEmpty(),
            dataNascimento = doc.getString("dataNascimento"),
            telefone = doc.getString("telefone")
        )
    }

    override fun sair() = auth.signOut()
}
