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
        telefone: String?
    ): Result<Unit> {
        cliente = Cliente("demo", nome, email, dataNascimento, telefone)
        return entrar(email, senha)
    }

    override suspend fun clienteAtual(): Cliente? = cliente

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

object DadosDemo {
    val categorias = listOf(
        Categoria("historia", "História e Cultura", "museu", "historia"),
        Categoria("praias", "Praias", "praia", "praias"),
        Categoria("gastronomia", "Gastronomia", "garfo", "gastronomia"),
        Categoria("arte", "Arte", "paleta", "arte")
    )

    val locais = listOf(
        Local(
            id = "marco-zero",
            titulo = "Marco Zero",
            descricao = "Praça no coração do Recife Antigo, ponto de partida para conhecer o bairro.",
            endereco = "Praça Rio Branco, Recife Antigo",
            custoAcesso = "Gratuito",
            horarioFuncionamento = "Aberto 24h",
            contato = "-",
            latitude = -8.0631,
            longitude = -34.8711,
            categoriaIds = listOf("historia")
        ),
        Local(
            id = "boa-viagem",
            titulo = "Praia de Boa Viagem",
            descricao = "Principal praia urbana do Recife, com calçadão e piscinas naturais na maré baixa.",
            endereco = "Av. Boa Viagem, Boa Viagem",
            custoAcesso = "Gratuito",
            horarioFuncionamento = "Aberto 24h",
            contato = "-",
            latitude = -8.1196,
            longitude = -34.8970,
            categoriaIds = listOf("praias")
        ),
        Local(
            id = "brennand",
            titulo = "Oficina Francisco Brennand",
            descricao = "Antiga olaria transformada em museu-ateliê com esculturas de Francisco Brennand.",
            endereco = "Av. Oficina Francisco Brennand, Várzea",
            custoAcesso = "Pago",
            horarioFuncionamento = "Ter a dom",
            contato = "-",
            latitude = -8.0425,
            longitude = -34.9688,
            categoriaIds = listOf("arte", "historia")
        ),
        Local(
            id = "mercado-sao-jose",
            titulo = "Mercado de São José",
            descricao = "Mercado público histórico com artesanato e comidas regionais.",
            endereco = "Praça Dom Vital, São José",
            custoAcesso = "Gratuito",
            horarioFuncionamento = "Seg a sáb",
            contato = "-",
            latitude = -8.0687,
            longitude = -34.8786,
            categoriaIds = listOf("gastronomia", "historia")
        )
    )
}
