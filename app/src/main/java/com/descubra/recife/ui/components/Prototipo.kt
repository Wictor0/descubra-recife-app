package com.descubra.recife.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.descubra.recife.R
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import com.descubra.recife.ui.theme.Laranja
import com.descubra.recife.ui.theme.Poppins
import com.descubra.recife.util.Imagens

// Peças visuais repetidas nas telas do protótipo: banner de cerâmica com avatar
// e busca, folha branca arredondada, cards de destino e círculos de categoria.

/** Círculo com a inicial do Cliente (o protótipo usa foto; a foto de perfil fica para depois). */
@Composable
fun AvatarCliente(inicial: String, tamanho: Dp, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            inicial,
            color = Laranja,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = (tamanho.value * 0.42f).sp
        )
    }
}

/**
 * Topo com a textura de cerâmica, avatar e campo "Pesquise aqui...".
 * Se [onBuscaClick] vier preenchido, o campo é só um atalho para a tela de busca.
 */
@Composable
fun CabecalhoBanner(
    inicial: String,
    onAvatarClick: () -> Unit,
    busca: String = "",
    onBuscaChange: (String) -> Unit = {},
    onBuscaClick: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_banner_recife),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCliente(inicial, 62.dp, onAvatarClick)
            Spacer(Modifier.width(14.dp))
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp))
                    .then(if (onBuscaClick != null) Modifier.clickable(onClick = onBuscaClick) else Modifier),
                shape = RoundedCornerShape(14.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.weight(1f)) {
                        if (busca.isEmpty()) {
                            Text("Pesquise aqui...", color = Color(0xFF9E9E9E), fontFamily = Poppins, fontSize = 14.sp)
                        }
                        if (onBuscaClick == null) {
                            BasicTextField(
                                value = busca,
                                onValueChange = onBuscaChange,
                                singleLine = true,
                                textStyle = TextStyle(color = Color(0xFF1E1E1E), fontFamily = Poppins, fontSize = 14.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                            )
                        }
                    }
                    IconeCanvas(TipoIcone.BUSCA, Color(0xFF9E9E9E), 20.dp)
                }
            }
        }
    }
}

/** Folha branca de cantos arredondados que sobe por cima do banner. */
@Composable
fun FolhaConteudo(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-48).dp),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(top = 14.dp, bottom = 110.dp), content = content)
    }
}

@Composable
fun BotaoVoltarCirculo(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Laranja)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        IconeCanvas(TipoIcone.VOLTAR, Color.White, 18.dp)
    }
}

@Composable
fun FotoLocal(local: Local, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Imagens.doLocal(local.id) ?: R.drawable.bg_banner_recife),
        contentDescription = local.titulo,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

/** Card de "Destinos populares": foto com degradê e o nome legível por cima. */
@Composable
fun CardDestino(local: Local, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(0.74f)
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        FotoLocal(local, Modifier.fillMaxSize())
        Legenda(local, tituloSp = 12f, Modifier.align(Alignment.BottomStart), maxLinhas = 4)
    }
}

/** Card largo de "Recomendado". */
@Composable
fun CardRecomendado(local: Local, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(270.dp)
            .height(170.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        FotoLocal(local, Modifier.fillMaxSize())
        Legenda(local, tituloSp = 17f, Modifier.align(Alignment.BottomStart))
    }
}

/** Nome e bairro em branco sobre um degradê escuro, para ler em qualquer foto. */
@Composable
private fun Legenda(local: Local, tituloSp: Float, modifier: Modifier, maxLinhas: Int = 2) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))))
            .padding(start = (tituloSp * 0.75f).dp, end = (tituloSp * 0.6f).dp, top = 24.dp, bottom = (tituloSp * 0.7f).dp)
    ) {
        Text(
            local.titulo,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = tituloSp.sp,
            lineHeight = (tituloSp * 1.2f).sp,
            color = Color.White,
            maxLines = maxLinhas,
            overflow = TextOverflow.Ellipsis
        )
        val lugar = local.bairro.ifBlank { local.endereco }
        if (lugar.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Filled.Place, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size((tituloSp * 0.85f).dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    lugar,
                    fontFamily = Poppins,
                    fontSize = (tituloSp * 0.8f).sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Círculo colorido de categoria com o nome embaixo. */
@Composable
fun CirculoCategoria(categoria: Categoria, selecionada: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .widthIn(min = 100.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        val icone = Imagens.daCategoria(categoria.slug)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (icone == null) Laranja else Color.Transparent)
                .then(if (selecionada) Modifier.border(3.dp, Laranja, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (icone != null) {
                Image(
                    painterResource(icone),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (selecionada) 4.dp else 0.dp)
                        .clip(CircleShape)
                )
            } else {
                Text(categoria.nome.take(1), color = Color.White, fontSize = 28.sp, fontFamily = Poppins)
            }
        }
        Text(
            categoria.nome,
            fontFamily = Poppins,
            fontSize = 13.sp,
            softWrap = false,
            fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Botão pílula laranja do protótipo ("Adicione", "Atualizar perfil", "COMO CHEGAR"). */
@Composable
fun BotaoPilula(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = Laranja,
    corTexto: Color = Color.White,
    habilitado: Boolean = true,
    fontSize: Int = 16,
    negrito: Boolean = false
) {
    Surface(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(50),
        color = if (habilitado) cor else cor.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Box(Modifier.padding(horizontal = 22.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                texto,
                color = corTexto,
                fontFamily = Poppins,
                fontSize = fontSize.sp,
                fontWeight = if (negrito) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
