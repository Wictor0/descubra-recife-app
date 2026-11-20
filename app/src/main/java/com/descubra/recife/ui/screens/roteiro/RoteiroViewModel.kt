package com.descubra.recife.ui.screens.roteiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.AvaliacaoRepository
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.data.repository.RoteiroDoCliente
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import com.descubra.recife.ui.screens.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ItemRoteiro(val local: Local, val avaliacoes: Int?)

/** De onde veio o roteiro que está na tela. */
enum class Sincronia { SINCRONIZANDO, NA_NUVEM, SO_NO_APARELHO }

/** Filtros da tela, separados do ViewModel para poderem ser testados. */
data class FiltrosRoteiro(val categoria: String? = null, val alfabetica: Boolean = false, val busca: String = "") {
    fun aplicar(locais: List<Local>, categoriasDoLocal: Map<String, List<String>>): List<Local> {
        var lista = locais
        if (categoria != null) lista = lista.filter { categoria in categoriasDoLocal[it.id].orEmpty() }
        if (busca.isNotBlank()) lista = lista.filter { HomeUiState.combina(it, busca) }
        if (alfabetica) lista = lista.sortedBy { it.titulo }
        return lista
    }
}

/** Uma linha do modal "Adicionar locais". */
data class OpcaoAdicionar(val local: Local, val noRoteiro: Boolean)

/** Lista do modal: todo o catálogo, com a busca aplicada e marcando o que já está no roteiro. */
object OpcoesAdicionar {
    fun listar(catalogo: List<Local>, idsNoRoteiro: Set<String>, busca: String): List<OpcaoAdicionar> =
        catalogo
            .filter { busca.isBlank() || HomeUiState.combina(it, busca) || HomeUiState.contemTexto(it.bairro, busca) }
            .map { OpcaoAdicionar(it, it.id in idsNoRoteiro) }
}

data class RoteiroUiState(
    val itens: List<ItemRoteiro> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val filtro: String? = null,
    val alfabetica: Boolean = false,
    val busca: String = "",
    val vazio: Boolean = true,
    /** Quantos locais o roteiro tem (sem filtro nem busca). */
    val totalNoRoteiro: Int = 0,
    val sincronia: Sincronia = Sincronia.SINCRONIZANDO,
    /** Horário da última alteração do roteiro (em qualquer aparelho). */
    val alteradoEm: Long? = null,
    /** Locais do catálogo para o modal "Adicionar locais". */
    val opcoes: List<OpcaoAdicionar> = emptyList(),
    val buscaModal: String = ""
) {
    /** Linha embaixo de "Seu Roteiro": de quando é a versão que está na tela. */
    fun textoVersao(fuso: TimeZone = TimeZone.getDefault()): String {
        val quando = alteradoEm?.let {
            SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).apply { timeZone = fuso }.format(Date(it))
        }
        return when {
            sincronia == Sincronia.SINCRONIZANDO -> "Sincronizando com a nuvem..."
            quando == null -> ""
            sincronia == Sincronia.SO_NO_APARELHO -> "Sem internet · versão de $quando"
            else -> "Atualizado em $quando"
        }
    }
}

class RoteiroViewModel(
    private val roteiro: RoteiroDoCliente,
    private val catalogo: CatalogoRepository,
    private val avaliacoes: AvaliacaoRepository
) : ViewModel() {

    private val categorias = MutableStateFlow<List<Categoria>>(emptyList())
    private val contagem = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val filtros = MutableStateFlow(FiltrosRoteiro())
    private val sincronia = MutableStateFlow(Sincronia.SINCRONIZANDO)
    private val todosOsLocais = MutableStateFlow<List<Local>>(emptyList())
    private val buscaModal = MutableStateFlow("")
    private val modal = combine(todosOsLocais, buscaModal) { locais, busca -> locais to busca }

    val estado: StateFlow<RoteiroUiState> = combine(
        roteiro.atual, categorias, contagem, combine(filtros, sincronia) { f, s -> f to s }, modal
    ) { atual, cats, cont, (f, sinc), (catalogo, buscaNoModal) ->
        val locais = atual?.locais.orEmpty()
        val porLocal = catalogo.associate { it.id to it.categoriaIds }
        RoteiroUiState(
            itens = f.aplicar(locais, porLocal).map { ItemRoteiro(it, cont[it.id]) },
            categorias = cats,
            filtro = f.categoria,
            alfabetica = f.alfabetica,
            busca = f.busca,
            vazio = locais.isEmpty(),
            totalNoRoteiro = locais.size,
            sincronia = sinc,
            alteradoEm = atual?.dataGeracao,
            opcoes = OpcoesAdicionar.listar(catalogo, locais.map { it.id }.toSet(), buscaNoModal),
            buscaModal = buscaNoModal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoteiroUiState())

    init {
        viewModelScope.launch {
            runCatching {
                categorias.value = catalogo.categorias()
                todosOsLocais.value = catalogo.locais()
            }
        }
        viewModelScope.launch {
            roteiro.locais.collect { locais ->
                locais.filter { it.id !in contagem.value }.forEach { local ->
                    runCatching { avaliacoes.avaliacoesDoLocal(local.id).size }
                        .onSuccess { n -> contagem.update { it + (local.id to n) } }
                }
            }
        }
    }

    /** Chamado toda vez que a aba Roteiro aparece: busca a versão mais atual na nuvem. */
    fun aoAbrir() {
        sincronia.value = Sincronia.SINCRONIZANDO
        viewModelScope.launch {
            sincronia.value = if (roteiro.sincronizar()) Sincronia.NA_NUVEM else Sincronia.SO_NO_APARELHO
        }
    }

    /** Enquanto a aba estiver aberta, aplica na hora o que mudar na nuvem. */
    suspend fun acompanhar() {
        roteiro.acompanhar().collect { sincronia.value = Sincronia.NA_NUVEM }
    }

    fun filtrar(categoriaId: String?) = filtros.update { it.copy(categoria = categoriaId) }

    fun alternarOrdem() = filtros.update { it.copy(alfabetica = !it.alfabetica) }

    fun buscar(termo: String) = filtros.update { it.copy(busca = termo) }

    fun buscarNoModal(termo: String) {
        buscaModal.value = termo
    }

    /** "+" do modal: põe o local no roteiro; tocar de novo (✓) tira. */
    fun alternarNoRoteiro(opcao: OpcaoAdicionar) {
        viewModelScope.launch {
            if (opcao.noRoteiro) roteiro.remover(opcao.local.id) else roteiro.adicionar(opcao.local)
        }
    }
}
