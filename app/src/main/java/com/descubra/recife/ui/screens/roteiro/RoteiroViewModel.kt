package com.descubra.recife.ui.screens.roteiro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.AvaliacaoRepository
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.data.repository.RoteiroDoCliente
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemRoteiro(val local: Local, val avaliacoes: Int?)

data class RoteiroUiState(
    val itens: List<ItemRoteiro> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val filtro: String? = null,
    val alfabetica: Boolean = false,
    val vazio: Boolean = true
)

class RoteiroViewModel(
    private val roteiro: RoteiroDoCliente,
    private val catalogo: CatalogoRepository,
    private val avaliacoes: AvaliacaoRepository
) : ViewModel() {

    private val categorias = MutableStateFlow<List<Categoria>>(emptyList())
    /** id do local -> categorias dele (a cópia do Room não guarda isso). */
    private val categoriasDoLocal = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    private val contagem = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val filtro = MutableStateFlow<String?>(null)
    private val alfabetica = MutableStateFlow(false)

    val estado: StateFlow<RoteiroUiState> = combine(
        roteiro.locais, categorias, categoriasDoLocal, contagem, combine(filtro, alfabetica) { f, a -> f to a }
    ) { locais, cats, porLocal, cont, (f, alfa) ->
        var lista = locais
        if (f != null) lista = lista.filter { f in porLocal[it.id].orEmpty() }
        if (alfa) lista = lista.sortedBy { it.titulo }
        RoteiroUiState(
            itens = lista.map { ItemRoteiro(it, cont[it.id]) },
            categorias = cats,
            filtro = f,
            alfabetica = alfa,
            vazio = locais.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoteiroUiState())

    init {
        viewModelScope.launch {
            roteiro.sincronizar()
            runCatching {
                categorias.value = catalogo.categorias()
                categoriasDoLocal.value = catalogo.locais().associate { it.id to it.categoriaIds }
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

    fun filtrar(categoriaId: String?) {
        filtro.value = categoriaId
    }

    fun alternarOrdem() = alfabetica.update { !it }
}
