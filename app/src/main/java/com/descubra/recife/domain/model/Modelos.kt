package com.descubra.recife.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Entidades da seção 9 (Arquitetura de Dados) do Documento de Arquitetura.
// As associativas Categoria_Local e Roteiro_Local viram listas de ids.

data class Cliente(
    val id: String,
    val nome: String,
    val email: String,
    val dataNascimento: String? = null,
    val telefone: String? = null,
    val genero: String? = null,
    /** Foto de perfil: URL (foto da conta Google) ou JPEG em Base64 (escolhida no app). */
    val foto: String? = null
) {
    val primeiroNome: String get() = nome.trim().substringBefore(' ').ifBlank { "Visitante" }
    val inicial: String get() = primeiroNome.take(1).uppercase()
}

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
    val imagens: List<String> = emptyList(),
    /** "popular" ou "recomendado": em qual seção da tela inicial o local aparece. */
    val destaque: String? = null,
    /** Bairro, mostrado nos cards (o endereço completo fica no detalhe). */
    val bairro: String = "",
    /** Link do cardápio online (restaurantes); null nos outros locais. */
    val cardapio: String? = null
)

data class Avaliacao(
    val id: String,
    val clienteId: String,
    val localId: String,
    val nota: Int,
    val comentario: String,
    /** Quando a avaliação foi feita (ms). */
    val data: Long,
    /** Nome de quem avaliou, gravado junto (o perfil do Cliente é privado). */
    val clienteNome: String = "",
    /** Quando foi editada pela última vez; null se nunca foi. */
    val editadoEm: Long? = null
) {
    init {
        require(nota in 1..5) { "A nota deve ser de 1 a 5" }
    }

    /** Só quem escreveu pode editar ou excluir. */
    fun eDe(clienteId: String?): Boolean = clienteId != null && clienteId == this.clienteId

    /** Nova versão editada: mantém autor e data original e registra quando mudou. */
    fun editada(nota: Int, comentario: String, agora: Long): Avaliacao =
        copy(nota = nota, comentario = comentario.trim(), editadoEm = agora)

    /** "Por Ana · 27/11/2026" e, se editada, "· editada em 27/11/2026 às 21:10". */
    fun descricao(fuso: TimeZone = TimeZone.getDefault()): String {
        val autor = clienteNome.ifBlank { "Visitante" }
        val dia = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply { timeZone = fuso }
        val diaHora = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).apply { timeZone = fuso }
        val base = "$autor · ${dia.format(Date(data))}"
        return editadoEm?.let { "$base · editada em ${diaHora.format(Date(it))}" } ?: base
    }
}

data class Roteiro(
    val id: String,
    val clienteId: String,
    val dataGeracao: Long,
    val locais: List<Local>
)

/** Regras do roteiro do Cliente, separadas para poderem ser testadas sem Android. */
object RoteiroRegras {
    /** O roteiro do protótipo é um só por Cliente ("Seu Roteiro"). */
    const val ID_ROTEIRO_ATUAL = "atual"

    fun comLocal(locais: List<Local>, local: Local): List<Local> =
        if (locais.any { it.id == local.id }) locais else locais + local

    fun semLocal(locais: List<Local>, localId: String): List<Local> =
        locais.filterNot { it.id == localId }
}
