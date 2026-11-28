package com.descubra.recife.ui.screens.roteiro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.descubra.recife.ui.components.BotaoPilula
import com.descubra.recife.ui.components.BotaoVoltarCirculo
import com.descubra.recife.ui.components.CabecalhoBanner
import com.descubra.recife.ui.components.FolhaConteudo
import com.descubra.recife.ui.components.FotoLocal
import com.descubra.recife.ui.theme.Laranja
import com.descubra.recife.ui.theme.Poppins
import com.descubra.recife.ui.theme.SobreLaranja
import com.descubra.recife.util.Mapas

/** "Seu Roteiro" (protótipo, telas 9 e 10). */
@Composable
fun RoteiroScreen(
    viewModel: RoteiroViewModel,
    inicialCliente: String,
    fotoCliente: Any?,
    onAvatarClick: () -> Unit,
    onVoltar: () -> Unit,
    onLocalClick: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var modalAberto by rememberSaveable { mutableStateOf(false) }

    if (modalAberto) {
        ModalAdicionarLocais(
            opcoes = estado.opcoes,
            totalNoRoteiro = estado.totalNoRoteiro,
            busca = estado.buscaModal,
            onBusca = viewModel::buscarNoModal,
            onAlternar = viewModel::alternarNoRoteiro,
            onFechar = { modalAberto = false; viewModel.buscarNoModal("") }
        )
    }

    // Toda vez que a aba aparece: puxa a versão mais atual da nuvem e fica
    // escutando mudanças enquanto ela estiver na tela.
    LaunchedEffect(Unit) {
        viewModel.aoAbrir()
        viewModel.acompanhar()
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // A busca daqui procura nos locais do roteiro.
        CabecalhoBanner(
            inicial = inicialCliente,
            onAvatarClick = onAvatarClick,
            foto = fotoCliente,
            busca = estado.busca,
            onBuscaChange = viewModel::buscar
        )
        FolhaConteudo {
            BotaoVoltarCirculo(onVoltar, Modifier.padding(start = 16.dp))
            Column(Modifier.padding(horizontal = 36.dp)) {
                Text("Seu Roteiro", fontFamily = Poppins, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    estado.textoVersao(),
                    fontFamily = Poppins,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (estado.vazio) {
                    Text(
                        "Conheça os lugares mais incríveis de Recife e Região.",
                        fontFamily = Poppins,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (estado.vazio) {
                Column(
                    Modifier.fillMaxWidth().padding(top = 160.dp, bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Você ainda não tem um roteiro", fontFamily = Poppins, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Que tal começarmos essa jornada juntos?", fontFamily = Poppins, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    BotaoPilula("Adicione", { modalAberto = true }, Modifier.width(150.dp).padding(top = 6.dp), fontSize = 18)
                }
                return@FolhaConteudo
            }

            // Três botões do mesmo tamanho; nome longo de categoria vira "Gastron…"
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var abrirFiltro by remember { mutableStateOf(false) }
                Box(Modifier.weight(1f)) {
                    PilulaAcao(
                        Icons.Filled.FilterAlt,
                        estado.categorias.find { it.id == estado.filtro }?.nome ?: "Filtros",
                        Modifier.fillMaxWidth()
                    ) { abrirFiltro = true }
                    DropdownMenu(expanded = abrirFiltro, onDismissRequest = { abrirFiltro = false }) {
                        DropdownMenuItem(text = { Text("Todos", fontFamily = Poppins) }, onClick = { viewModel.filtrar(null); abrirFiltro = false })
                        estado.categorias.forEach { c ->
                            DropdownMenuItem(text = { Text(c.nome, fontFamily = Poppins) }, onClick = { viewModel.filtrar(c.id); abrirFiltro = false })
                        }
                    }
                }
                PilulaAcao(Icons.AutoMirrored.Filled.Sort, if (estado.alfabetica) "A-Z" else "Ordem", Modifier.weight(1f), viewModel::alternarOrdem)
                PilulaAcao(Icons.Filled.Add, "Locais", Modifier.weight(1f)) { modalAberto = true }
            }

            if (estado.itens.isEmpty()) {
                Text(
                    if (estado.busca.isNotBlank()) "Nenhum local do roteiro com esse nome." else "Nenhum local do roteiro nessa categoria.",
                    fontFamily = Poppins,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 36.dp)
                )
            }
            estado.itens.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onLocalClick(item.local.id) }
                        .padding(horizontal = 32.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FotoLocal(item.local, Modifier.size(92.dp).clip(RoundedCornerShape(14.dp)))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.local.titulo,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        item.avaliacoes?.let { n ->
                            Text(
                                if (n == 1) "1 avaliação" else "$n avaliações",
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            onClick = { Mapas.abrirComoChegar(context, item.local) },
                            color = Laranja,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                "COMO CHEGAR",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SobreLaranja,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
            Text(
                "Para tirar um local do roteiro, abra-o e toque em \"Remover do roteiro\".",
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun PilulaAcao(icone: ImageVector, texto: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Laranja, shape = RoundedCornerShape(50), modifier = modifier) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icone, contentDescription = null, tint = SobreLaranja, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                texto,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SobreLaranja,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Modal com todos os locais do catálogo e um "+" em cada um para montar o roteiro rápido. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalAdicionarLocais(
    opcoes: List<OpcaoAdicionar>,
    totalNoRoteiro: Int,
    busca: String,
    onBusca: (String) -> Unit,
    onAlternar: (OpcaoAdicionar) -> Unit,
    onFechar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Adicionar ao roteiro", fontFamily = Poppins, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    // Conta o roteiro inteiro, não só o que aparece na busca
                    val total = totalNoRoteiro
                    Text(
                        if (total == 1) "1 local no seu roteiro" else "$total locais no seu roteiro",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BotaoPilula("Pronto", onFechar, fontSize = 14)
            }
            OutlinedTextField(
                value = busca,
                onValueChange = onBusca,
                placeholder = { Text("Buscar local ou bairro", fontFamily = Poppins) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Laranja),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (opcoes.isEmpty()) {
                item { Text("Nenhum local encontrado.", fontFamily = Poppins, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(opcoes, key = { it.local.id }) { opcao ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onAlternar(opcao) }
                        .padding(vertical = 4.dp)
                ) {
                    FotoLocal(opcao.local, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            opcao.local.titulo,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (opcao.local.bairro.isNotBlank()) {
                            Text(opcao.local.bairro, fontFamily = Poppins, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    // "+" laranja para adicionar; "✓" preenchido quando já está no roteiro
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (opcao.noRoteiro) Laranja else Color.Transparent)
                            .border(2.dp, Laranja, CircleShape)
                    ) {
                        Icon(
                            if (opcao.noRoteiro) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = if (opcao.noRoteiro) "Tirar ${opcao.local.titulo} do roteiro" else "Adicionar ${opcao.local.titulo}",
                            tint = if (opcao.noRoteiro) SobreLaranja else Laranja
                        )
                    }
                }
            }
        }
    }
}
