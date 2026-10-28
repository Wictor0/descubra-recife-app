package com.descubra.recife.data.repository

import com.descubra.recife.domain.model.Avaliacao
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Cliente
import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Contratos da camada de dados (seção 8). As telas/ViewModels só conhecem
// estas interfaces; as implementações ficam em Firebase* e em data/demo.

interface AuthRepository {
    /** uid do Cliente logado, ou null. */
    val clienteAtualId: StateFlow<String?>
    suspend fun entrar(email: String, senha: String): Result<Unit>
    suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        dataNascimento: String?,
        genero: String?
    ): Result<Unit>
    suspend fun clienteAtual(): Cliente?
    suspend fun atualizarPerfil(nome: String, dataNascimento: String?): Result<Unit>
    /** Envia o e-mail de troca de senha do Firebase. */
    suspend fun redefinirSenha(): Result<Unit>
    fun sair()
}

interface CatalogoRepository {
    suspend fun categorias(): List<Categoria>
    suspend fun locais(categoriaId: String? = null): List<Local>
    suspend fun local(id: String): Local?
}

interface AvaliacaoRepository {
    suspend fun avaliacoesDoLocal(localId: String): List<Avaliacao>
    suspend fun avaliar(avaliacao: Avaliacao)
}

interface RoteiroRepository {
    fun roteiros(clienteId: String): Flow<List<Roteiro>>
    suspend fun salvar(roteiro: Roteiro)
    suspend fun excluir(clienteId: String, roteiroId: String)
    /** Puxa do Firestore os roteiros feitos em outro aparelho (RA-005). */
    suspend fun sincronizar(clienteId: String)
}

/** Lado remoto dos roteiros. É opcional: no modo demonstração não existe. */
interface RoteiroRemoto {
    suspend fun salvar(roteiro: Roteiro)
    suspend fun excluir(clienteId: String, roteiroId: String)
    suspend fun listar(clienteId: String): List<Roteiro>
}
