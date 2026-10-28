package com.descubra.recife.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.CatalogoRepository
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

data class HomeUiState(
    val categorias: List<Categoria> = emptyList(),
    val locais: List<Local> = emptyList(),
    val categoriaSelecionada: String? = null,
    val busca: String = "",
    val carregando: Boolean = true,
    val erro: String? = null
) {
    private val daCategoria: List<Local>
        get() = categoriaSelecionada?.let { id -> locais.filter { id in it.categoriaIds } } ?: locais

    /** Resultado da busca/filtro; null quando a tela mostra as seções padrão. */
    val resultados: List<Local>?
        get() = when {
            busca.isNotBlank() -> daCategoria.filter { combina(it, busca) }
            categoriaSelecionada != null -> daCategoria
            else -> null
        }

    val populares: List<Local>
        get() = locais.filter { it.destaque == "popular" }.ifEmpty { locais.take(6) }

    val recomendados: List<Local>
        get() = locais.filter { it.destaque == "recomendado" }.ifEmpty { locais.drop(6) }

    companion object {
        /** Busca sem diferenciar maiúsculas nem acentos ("paco" acha "Paço"). */
        fun combina(local: Local, termo: String): Boolean {
            val t = normalizar(termo)
            return normalizar(local.titulo).contains(t) || normalizar(local.endereco).contains(t)
        }

        private fun normalizar(s: String) =
            Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase().trim()
    }
}

class HomeViewModel(private val catalogo: CatalogoRepository) : ViewModel() {

    private val _estado = MutableStateFlow(HomeUiState())
    val estado: StateFlow<HomeUiState> = _estado

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            runCatching { catalogo.categorias() to catalogo.locais() }
                .onSuccess { (categorias, locais) ->
                    _estado.update { it.copy(categorias = categorias, locais = locais, carregando = false) }
                }
                .onFailure { e ->
                    _estado.update { it.copy(carregando = false, erro = e.message ?: "Erro ao carregar locais") }
                }
        }
    }

    fun selecionarCategoria(id: String) = _estado.update {
        it.copy(categoriaSelecionada = if (it.categoriaSelecionada == id) null else id)
    }

    fun buscar(termo: String) = _estado.update { it.copy(busca = termo) }
}
