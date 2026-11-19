package com.descubra.recife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.platform.LocalFocusManager
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

/** Abas da barra inferior; [NENHUMA] para telas fora das abas (ex.: FAQ). */
enum class Aba { BUSCAR, INICIO, ROTEIRO, NENHUMA }

/** Barra flutuante Buscar / Início / Roteiro (extraída da tela de FAQ). */
@Composable
fun BarraNavegacao(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onItineraryClick: () -> Unit,
    abaAtual: Aba = Aba.NENHUMA
) {
    // Ao trocar de aba, fecha o teclado e tira o foco do campo de busca.
    val foco = LocalFocusManager.current
    fun ir(acao: () -> Unit) = { foco.clearFocus(); acao() }
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
            ItemBarra("Buscar", TipoIcone.BUSCA, abaAtual == Aba.BUSCAR, onClick = ir(onSearchClick))
            ItemBarra("Início", TipoIcone.INICIO, abaAtual == Aba.INICIO, onClick = ir(onHomeClick))
            ItemBarra("Roteiro", TipoIcone.MAPA, abaAtual == Aba.ROTEIRO, onClick = ir(onItineraryClick))
        }
    }
}

@Composable
private fun ItemBarra(label: String, icone: TipoIcone, selecionada: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selecionada) Color.White.copy(alpha = 0.22f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        IconeCanvas(tipo = icone, tint = Color.White, size = 22.dp)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/** Esqueleto das telas principais: conteúdo + barra flutuante no rodapé. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TelaComBarra(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onItineraryClick: () -> Unit,
    abaAtual: Aba = Aba.NENHUMA,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.fillMaxSize(), content = content)
        }
        // A barra fica sempre visível: com o teclado aberto ela sobe junto com ele,
        // para dar para trocar de aba mesmo digitando na busca.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(bottom = if (WindowInsets.isImeVisible) 16.dp else 0.dp)
        ) {
            BarraNavegacao(onHomeClick, onSearchClick, onItineraryClick, abaAtual)
        }
    }
}
