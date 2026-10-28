package com.descubra.recife.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.descubra.recife.domain.model.Local
import com.descubra.recife.ui.components.BotaoPilula
import com.descubra.recife.ui.components.CabecalhoBanner
import com.descubra.recife.ui.components.CardDestino
import com.descubra.recife.ui.components.CardRecomendado
import com.descubra.recife.ui.components.CirculoCategoria
import com.descubra.recife.ui.components.FolhaConteudo
import com.descubra.recife.ui.theme.Poppins

/**
 * Tela inicial (protótipo, tela 5). A aba "Buscar" é esta mesma tela com o
 * campo de busca já focado (protótipo, tela 6).
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    inicialCliente: String,
    focarBusca: Boolean,
    onAvatarClick: () -> Unit,
    onLocalClick: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val foco = remember { FocusRequester() }
    LaunchedEffect(focarBusca) {
        if (focarBusca) runCatching { foco.requestFocus() }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CabecalhoBanner(
            inicial = inicialCliente,
            onAvatarClick = onAvatarClick,
            busca = estado.busca,
            onBuscaChange = viewModel::buscar,
            focusRequester = foco
        )
        FolhaConteudo {
            Column(Modifier.padding(horizontal = 32.dp, vertical = 16.dp)) {
                Text("Explore Pernambuco", fontFamily = Poppins, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Descubra os melhores destinos e experiências",
                    fontFamily = Poppins,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(estado.categorias, key = { it.id }) { categoria ->
                    CirculoCategoria(
                        categoria,
                        selecionada = estado.categoriaSelecionada == categoria.id,
                        onClick = { viewModel.selecionarCategoria(categoria.id) }
                    )
                }
            }

            when {
                estado.carregando -> Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                estado.erro != null -> Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Não foi possível carregar os destinos.", fontFamily = Poppins)
                    Text(estado.erro!!, fontFamily = Poppins, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    BotaoPilula("Tentar de novo", viewModel::carregar)
                }
                estado.resultados != null -> {
                    val titulo = when {
                        estado.busca.isNotBlank() -> "Resultados"
                        else -> estado.categorias.find { it.id == estado.categoriaSelecionada }?.nome ?: "Destinos"
                    }
                    TituloSecao(titulo)
                    val lista = estado.resultados!!
                    if (lista.isEmpty()) {
                        Text(
                            "Nenhum destino encontrado.",
                            fontFamily = Poppins,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    } else {
                        GradeDestinos(lista, onLocalClick)
                    }
                }
                else -> {
                    TituloSecao("Destinos populares")
                    GradeDestinos(estado.populares, onLocalClick)
                    if (estado.recomendados.isNotEmpty()) {
                        TituloSecao("Recomendado")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 28.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(estado.recomendados, key = { it.id }) { local ->
                                CardRecomendado(local) { onLocalClick(local.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TituloSecao(texto: String) {
    Text(
        texto,
        fontFamily = Poppins,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 14.dp)
    )
}

@Composable
private fun GradeDestinos(locais: List<Local>, onLocalClick: (String) -> Unit) {
    Column(
        Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Grade 3 colunas como no protótipo; a próxima seção aparece logo abaixo
        locais.chunked(3).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                linha.forEach { local ->
                    CardDestino(local, onClick = { onLocalClick(local.id) }, modifier = Modifier.weight(1f))
                }
                repeat(3 - linha.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
