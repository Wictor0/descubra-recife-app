package com.descubra.recife.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.descubra.recife.ui.theme.Laranja

/** Barra flutuante Buscar / Início / Roteiro (extraída da tela de FAQ). */
@Composable
fun BarraNavegacao(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onItineraryClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .offset(y = 8.dp)
            .height(62.dp),
        shape = RoundedCornerShape(28.dp),
        color = Laranja,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemBarra("Buscar", TipoIcone.BUSCA, onClick = onSearchClick)
            ItemBarra("Início", TipoIcone.INICIO, onClick = onHomeClick)
            ItemBarra("Roteiro", TipoIcone.MAPA, onClick = onItineraryClick)
        }
    }
}

@Composable
private fun ItemBarra(label: String, icone: TipoIcone, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp)
    ) {
        IconeCanvas(tipo = icone, tint = Color.White, size = 22.dp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

/** Esqueleto das telas principais: conteúdo + barra flutuante no rodapé. */
@Composable
fun TelaComBarra(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onItineraryClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize(), content = content)
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            BarraNavegacao(onHomeClick, onSearchClick, onItineraryClick)
        }
    }
}
