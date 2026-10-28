package com.descubra.recife.data.repository

import com.descubra.recife.data.remote.FirestoreColecoes
import com.descubra.recife.domain.model.Cliente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
        genero: String?
    ): Result<Unit> = runCatching {
        val uid = auth.createUserWithEmailAndPassword(email, senha).await().user!!.uid
        perfil(uid).set(
            mapOf(
                "nome" to nome,
                "email" to email,
                "dataNascimento" to dataNascimento,
                "genero" to genero
            )
        ).await()
        Unit
    }

    override suspend fun clienteAtual(): Cliente? {
        val usuario = auth.currentUser ?: return null
        val doc = perfil(usuario.uid).get().await()
        return Cliente(
            id = usuario.uid,
            nome = doc.getString("nome").orEmpty(),
            email = doc.getString("email") ?: usuario.email.orEmpty(),
            dataNascimento = doc.getString("dataNascimento"),
            telefone = doc.getString("telefone"),
            genero = doc.getString("genero")
        )
    }

    override suspend fun atualizarPerfil(nome: String, dataNascimento: String?): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Faça login novamente")
        perfil(uid).set(mapOf("nome" to nome, "dataNascimento" to dataNascimento), SetOptions.merge()).await()
        Unit
    }

    override suspend fun redefinirSenha(): Result<Unit> = runCatching {
        val email = auth.currentUser?.email ?: error("Faça login novamente")
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    override fun sair() = auth.signOut()

    private fun perfil(uid: String) = db.collection(FirestoreColecoes.CLIENTES).document(uid)
}
