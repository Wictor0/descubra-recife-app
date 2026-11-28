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
    /** Login com a conta Google: recebe o ID token obtido pelo Credential Manager. */
    suspend fun entrarComGoogle(idToken: String): Result<Unit>
    suspend fun clienteAtual(): Cliente?
    suspend fun atualizarPerfil(nome: String, dataNascimento: String?): Result<Unit>
    /** Salva a foto de perfil (JPEG pequeno em Base64) no perfil do Cliente. */
    suspend fun atualizarFoto(jpegBase64: String): Result<Unit>
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
    /** Cria a avaliação, ou grava por cima a versão editada (mesmo id). */
    suspend fun avaliar(avaliacao: Avaliacao)
    suspend fun excluir(avaliacaoId: String)
}

interface RoteiroRepository {
    fun roteiros(clienteId: String): Flow<List<Roteiro>>
    suspend fun salvar(roteiro: Roteiro)
    suspend fun excluir(clienteId: String, roteiroId: String)
    /**
     * Sincroniza o aparelho com a nuvem (RA-005): sobe o que foi feito offline
     * e baixa a versão mais atual. Devolve false se não houve conexão.
     */
    suspend fun sincronizar(clienteId: String): Boolean
    /** Fica escutando a nuvem e aplica no aparelho as mudanças feitas em outro aparelho. */
    fun acompanhar(clienteId: String): Flow<Unit>
}

/** Lado remoto dos roteiros. É opcional: no modo demonstração não existe. */
interface RoteiroRemoto {
    suspend fun salvar(roteiro: Roteiro)
    suspend fun excluir(clienteId: String, roteiroId: String)
    /** Lê direto do servidor (falha se estiver sem internet). */
    suspend fun listar(clienteId: String): List<Roteiro>
    /** Emite a lista do servidor a cada mudança (publish/subscribe). */
    fun observar(clienteId: String): Flow<List<Roteiro>>
}
