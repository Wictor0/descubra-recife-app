package com.descubra.recife.data.demo

import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.data.repository.AvaliacaoRepository
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.domain.model.Avaliacao
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Cliente
import com.descubra.recife.domain.model.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Implementações em memória usadas enquanto o app não tem google-services.json.
// Servem para desenvolver as telas sem depender do Firebase.

class AuthRepositoryDemo : AuthRepository {
    private var cliente: Cliente? = null
    private val _clienteAtualId = MutableStateFlow<String?>(null)
    override val clienteAtualId: StateFlow<String?> = _clienteAtualId

    override suspend fun entrar(email: String, senha: String): Result<Unit> {
        if (email.isBlank() || senha.length < 6) {
            return Result.failure(IllegalArgumentException("E-mail ou senha inválidos"))
        }
        cliente = cliente ?: Cliente(id = "demo", nome = "Visitante", email = email)
        _clienteAtualId.value = "demo"
        return Result.success(Unit)
    }

    override suspend fun cadastrar(
        nome: String,
        email: String,
        senha: String,
        dataNascimento: String?,
        genero: String?
    ): Result<Unit> {
        cliente = Cliente("demo", nome, email, dataNascimento, genero = genero)
        return entrar(email, senha)
    }

    override suspend fun entrarComGoogle(idToken: String): Result<Unit> =
        Result.failure(IllegalStateException("Login com Google exige o Firebase configurado"))

    override suspend fun clienteAtual(): Cliente? = cliente

    override suspend fun atualizarPerfil(nome: String, dataNascimento: String?): Result<Unit> {
        cliente = cliente?.copy(nome = nome, dataNascimento = dataNascimento)
        return Result.success(Unit)
    }

    override suspend fun redefinirSenha(): Result<Unit> = Result.success(Unit)

    override fun sair() {
        _clienteAtualId.value = null
    }
}

class CatalogoRepositoryDemo : CatalogoRepository {
    override suspend fun categorias() = DadosDemo.categorias
    override suspend fun locais(categoriaId: String?) =
        if (categoriaId == null) DadosDemo.locais else DadosDemo.locais.filter { categoriaId in it.categoriaIds }
    override suspend fun local(id: String) = DadosDemo.locais.find { it.id == id }
}

class AvaliacaoRepositoryDemo : AvaliacaoRepository {
    private val avaliacoes = mutableListOf<Avaliacao>()
    override suspend fun avaliacoesDoLocal(localId: String) = avaliacoes.filter { it.localId == localId }
    override suspend fun avaliar(avaliacao: Avaliacao) {
        avaliacoes += avaliacao
    }
}

/** Amostra do catálogo de tools/seed/dados.json (o completo fica no Firestore). */
object DadosDemo {
    val categorias = listOf(
        Categoria("praias", "Praias", "praias", "praias"),
        Categoria("gastronomia", "Gastronomia", "gastronomia", "gastronomia"),
        Categoria("cultura", "Cultura", "cultura", "cultura"),
        Categoria("folia", "Folia", "folia", "folia"),
        Categoria("historia", "História", "historia", "historia")
    )

    val locais = listOf(
        Local(
            id = "marco-zero",
            titulo = "Praça do Marco Zero",
            descricao = "Praça no coração do Recife Antigo, ponto simbólico de fundação da cidade e palco do Carnaval.",
            endereco = "Av. Alfredo Lisboa, Recife - PE, 50030-150",
            custoAcesso = "Gratuito",
            horarioFuncionamento = "Aberto 24h",
            contato = "",
            latitude = -8.0631,
            longitude = -34.8711,
            categoriaIds = listOf("historia", "folia"),
            destaque = "popular"
        ),
        Local(
            id = "praia-boa-viagem",
            titulo = "Praia de Boa Viagem",
            descricao = "Principal praia urbana do Recife, com calçadão e piscinas naturais na maré baixa.",
            endereco = "Praia de Boa Viagem, Boa Viagem, Recife - PE",
            custoAcesso = "Gratuito",
            horarioFuncionamento = "Aberto 24h",
            contato = "",
            latitude = -8.1260,
            longitude = -34.8990,
            categoriaIds = listOf("praias"),
            destaque = "popular"
        ),
        Local(
            id = "bode-do-no",
            titulo = "Bode do Nô",
            descricao = "Restaurante tradicional de comida nordestina, famoso pela carne de bode.",
            endereco = "Rua Dr. João Guilherme Pontes Sobrinho, 245 - Boa Viagem, Recife - PE",
            custoAcesso = "Pago",
            horarioFuncionamento = "Diariamente",
            contato = "",
            latitude = -8.1225,
            longitude = -34.9010,
            categoriaIds = listOf("gastronomia"),
            destaque = "popular"
        ),
        Local(
            id = "paco-do-frevo",
            titulo = "Paço do Frevo",
            descricao = "Espaço dedicado ao frevo, Patrimônio Imaterial da Humanidade.",
            endereco = "Praça do Arsenal da Marinha, 91 - Recife - PE, 50030-360",
            custoAcesso = "Pago",
            horarioFuncionamento = "Terça a domingo",
            contato = "",
            latitude = -8.0612,
            longitude = -34.8719,
            categoriaIds = listOf("folia", "cultura"),
            destaque = "recomendado"
        )
    )
}
