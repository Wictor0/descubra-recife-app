package com.descubra.recife.ui.screens.roteiro

import androidx.compose.foundation.clickable
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
    onAvatarClick: () -> Unit,
    onBuscaClick: () -> Unit,
    onVoltar: () -> Unit,
    onAdicionar: () -> Unit,
    onLocalClick: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CabecalhoBanner(inicial = inicialCliente, onAvatarClick = onAvatarClick, onBuscaClick = onBuscaClick)
        FolhaConteudo {
            BotaoVoltarCirculo(onVoltar, Modifier.padding(start = 16.dp))
            Column(Modifier.padding(horizontal = 36.dp)) {
                Text("Seu Roteiro", fontFamily = Poppins, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
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
                    BotaoPilula("Adicione", onAdicionar, Modifier.width(150.dp).padding(top = 6.dp), fontSize = 18)
                }
                return@FolhaConteudo
            }

            Row(Modifier.padding(start = 34.dp, top = 12.dp, bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                var abrirFiltro by remember { mutableStateOf(false) }
                Box {
                    PilulaAcao(Icons.Filled.FilterAlt, estado.categorias.find { it.id == estado.filtro }?.nome ?: "Filtros") { abrirFiltro = true }
                    DropdownMenu(expanded = abrirFiltro, onDismissRequest = { abrirFiltro = false }) {
                        DropdownMenuItem(text = { Text("Todos", fontFamily = Poppins) }, onClick = { viewModel.filtrar(null); abrirFiltro = false })
                        estado.categorias.forEach { c ->
                            DropdownMenuItem(text = { Text(c.nome, fontFamily = Poppins) }, onClick = { viewModel.filtrar(c.id); abrirFiltro = false })
                        }
                    }
                }
                PilulaAcao(Icons.AutoMirrored.Filled.Sort, if (estado.alfabetica) "A-Z" else "Ordem", viewModel::alternarOrdem)
            }

            if (estado.itens.isEmpty()) {
                Text(
                    "Nenhum local do roteiro nessa categoria.",
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
private fun PilulaAcao(icone: ImageVector, texto: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Laranja, shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, contentDescription = null, tint = SobreLaranja, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(texto, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SobreLaranja)
        }
    }
}
