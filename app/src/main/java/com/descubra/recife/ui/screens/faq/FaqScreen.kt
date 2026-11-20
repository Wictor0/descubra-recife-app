package com.descubra.recife.ui.screens.faq

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.descubra.recife.ui.components.BotaoVoltarCirculo
import com.descubra.recife.ui.components.CabecalhoBanner
import com.descubra.recife.ui.components.FolhaConteudo
import com.descubra.recife.ui.screens.home.HomeUiState
import com.descubra.recife.ui.theme.Laranja
import com.descubra.recife.ui.theme.Poppins
import com.descubra.recife.ui.theme.SobreLaranja

data class FaqItemData(
    val number: String,
    val question: String,
    val answer: String
)

/** Perguntas da FAQ (texto do grupo, ajustado ao que o app faz hoje) e a busca nelas. */
object FaqConteudo {
    val perguntas = listOf(
        FaqItemData(
            "01",
            "Como crio um roteiro personalizado?",
            "Abra um ponto turístico e toque em \"Adicionar ao Roteiro\". Na aba Roteiro, o botão \"Adicionar locais\" mostra todos os lugares com um \"+\" ao lado para montar o roteiro mais rápido."
        ),
        FaqItemData(
            "02",
            "O aplicativo funciona sem internet / offline?",
            "Sim. O roteiro fica salvo no aparelho e abre mesmo sem conexão. O que você mudar offline é enviado para a sua conta assim que a internet voltar."
        ),
        FaqItemData(
            "03",
            "Como funcionam os horários e valores exibidos?",
            "Os horários de funcionamento são atualizados constantemente por nossa equipe. Já os valores de entrada e consumo são informativos e sujeitos a alterações pelo próprio estabelecimento."
        ),
        FaqItemData(
            "04",
            "Se eu trocar de celular, perco meu roteiro?",
            "Não. O roteiro fica guardado na sua conta: é só entrar com o mesmo e-mail ou conta Google em outro aparelho. A data e a hora embaixo de \"Seu Roteiro\" mostram de quando é a versão."
        ),
        FaqItemData(
            "05",
            "Como troco minha foto de perfil?",
            "Toque na sua foto (ou inicial) no topo da tela, depois em \"Ver perfil\" e no lápis em cima da foto. Quem entra com a conta Google já começa com a foto do Google."
        )
    )

    /** Filtra pela pergunta ou pela resposta, sem diferenciar acentos e maiúsculas. */
    fun buscar(termo: String): List<FaqItemData> =
        if (termo.isBlank()) perguntas
        else perguntas.filter { HomeUiState.contemTexto(it.question, termo) || HomeUiState.contemTexto(it.answer, termo) }
}

/**
 * FAQ com o mesmo visual das outras telas: banner com a foto do Cliente,
 * busca (que aqui procura nas perguntas), folha branca e barra de abas.
 * [onAvatarClick] null = tela aberta antes do login (sem avatar nem menu).
 */
@Composable
fun FaqScreen(
    inicialCliente: String,
    fotoCliente: Any?,
    onAvatarClick: (() -> Unit)?,
    onVoltar: () -> Unit
) {
    var busca by rememberSaveable { mutableStateOf("") }
    var aberta by rememberSaveable { mutableStateOf<String?>(null) }
    val itens = FaqConteudo.buscar(busca)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CabecalhoBanner(
            inicial = inicialCliente,
            onAvatarClick = onAvatarClick ?: {},
            foto = fotoCliente,
            busca = busca,
            onBuscaChange = { busca = it },
            mostrarAvatar = onAvatarClick != null
        )
        FolhaConteudo {
            BotaoVoltarCirculo(onVoltar, Modifier.padding(start = 16.dp))
            Column(Modifier.padding(horizontal = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Estamos aqui para ajudar você com tudo o que precisar no Descubra Recife",
                    fontFamily = Poppins,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Encontre respostas rápidas sobre o uso do aplicativo, a criação de roteiros e os atrativos de Recife. Use a busca no topo para filtrar as perguntas.",
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Perguntas frequentes",
                    fontFamily = Poppins,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 14.dp)
                )
                if (itens.isEmpty()) {
                    Text("Nenhuma pergunta encontrada para \"$busca\".", fontFamily = Poppins, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                itens.forEach { item ->
                    CartaoPergunta(item, aberta = aberta == item.number || busca.isNotBlank()) {
                        aberta = if (aberta == item.number) null else item.number
                    }
                }
            }
        }
    }
}

/** Pergunta que abre e fecha a resposta ao tocar. */
@Composable
private fun CartaoPergunta(item: FaqItemData, aberta: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(Laranja),
                contentAlignment = Alignment.Center
            ) {
                Text(item.number, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SobreLaranja)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.question, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                if (aberta) {
                    Text(
                        item.answer,
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            Icon(
                if (aberta) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (aberta) "Fechar resposta" else "Ver resposta",
                tint = Laranja
            )
        }
    }
}
