package com.descubra.recife.domain.model

// Entidades da seção 9 (Arquitetura de Dados) do Documento de Arquitetura.
// As associativas Categoria_Local e Roteiro_Local viram listas de ids.

data class Cliente(
    val id: String,
    val nome: String,
    val email: String,
    val dataNascimento: String? = null,
    val telefone: String? = null
)

data class Categoria(
    val id: String,
    val nome: String,
    val icone: String,
    val slug: String
)

data class Local(
    val id: String,
    val titulo: String,
    val descricao: String,
    val endereco: String,
    val custoAcesso: String,
    val horarioFuncionamento: String,
    val contato: String,
    val latitude: Double,
    val longitude: Double,
    val categoriaIds: List<String> = emptyList(),
    val imagens: List<String> = emptyList()
)

data class Avaliacao(
    val id: String,
    val clienteId: String,
    val localId: String,
    val nota: Int,
    val comentario: String,
    val data: Long
) {
    init {
        require(nota in 1..5) { "A nota deve ser de 1 a 5" }
    }
}

data class Roteiro(
    val id: String,
    val clienteId: String,
    val dataGeracao: Long,
    val locais: List<Local>
)
