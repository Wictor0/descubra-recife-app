package com.descubra.recife.ui.screens.faq

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.descubra.recife.R
import com.descubra.recife.ui.components.BarraNavegacao
import com.descubra.recife.ui.components.IconeCanvas
import com.descubra.recife.ui.components.TipoIcone

// Tela de FAQ original do grupo. Mudanças: pacote, e a barra/ícones foram
// movidos para ui/components para serem reaproveitados nas outras telas.

data class FaqItemData(
    val number: String,
    val question: String,
    val answer: String
)

@Composable
fun FaqScreen(
    isDarkMode: Boolean = false,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onItineraryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = onHomeClick
) {
    val accentOrange = Color(0xFFD97736)
    // DEFINIÇÃO DINÂMICA DE CORES PARA DARK MODE
    val lightBackground = if (isDarkMode) Color(0xFF121212) else Color(0xFFFAFAFA)
    val cardBg = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFAF8F5)
    val contentSurfaceBg = if (isDarkMode) Color(0xFF181818) else Color.White
    val textPrimaryColor = if (isDarkMode) Color.White else Color(0xFF1E293B)
    val textSecondaryColor = if (isDarkMode) Color(0xFFA0A0A0) else Color(0xFF64748B)
    val badgeBg = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF1E6DF)

    var searchQueryHeader by remember { mutableStateOf("") }
    var searchQueryContent by remember { mutableStateOf("") }

    val faqList = listOf(
        FaqItemData(
            number = "01",
            question = "Como crio um roteiro personalizado?",
            answer = "Navegue pelos pontos turísticos, selecione os locais que deseja visitar e toque em \"Adicionar ao Roteiro\"."
        ),
        FaqItemData(
            number = "02",
            question = "O aplicativo funciona sem internet / offline?",
            answer = "Você pode consultar seus roteiros salvos em PDF a qualquer momento, mesmo sem conexão com a internet."
        ),
        FaqItemData(
            number = "03",
            question = "Como funcionam os horários e valores exibidos?",
            answer = "Os horários de funcionamento são atualizados constantemente por nossa equipe. Já os valores de entrada e consumo são informativos e sujeitos a alterações pelo próprio estabelecimento."
        ),
        FaqItemData(
            number = "04",
            question = "Posso compartilhar meu roteiro com outras pessoas?",
            answer = "Sim! Ao exportar o seu roteiro em PDF, você pode enviá-lo facilmente via WhatsApp, e-mail ou salvar no seu aplicativo de arquivos para acessar a qualquer momento."
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- CAPA CHEIA PREENCHENDO O TOPO/STATUS BAR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_banner_recife),
                    contentDescription = "Fundo do Cabeçalho",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.foto_perfil),
                            contentDescription = "Foto de Perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(21.dp),
                        color = if (isDarkMode) Color(0xFF2C2C2C) else Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = searchQueryHeader,
                                onValueChange = { searchQueryHeader = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = TextStyle(color = textPrimaryColor, fontSize = 13.sp),
                                decorationBox = { innerTextField ->
                                    if (searchQueryHeader.isEmpty()) {
                                        Text("Pesquise aqui...", color = Color.Gray, fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            )
                            IconeCanvas(tipo = TipoIcone.BUSCA, tint = Color.Gray, size = 16.dp)
                        }
                    }
                }
            }

            // --- CONTEÚDO PRINCIPAL (COM PREENCHIMENTO COMPLETO) ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = contentSurfaceBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .padding(bottom = 90.dp) // Espaço para rolar o conteúdo por trás da barra sem cobrir os cards
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentOrange)
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        IconeCanvas(tipo = TipoIcone.VOLTAR, tint = Color.White, size = 16.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Estamos aqui para ajudar você com tudo o que precisar no Descubra Recife",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Encontre respostas rápidas sobre o uso do aplicativo, criação de roteiros personalizados e navegação pelos atrativos de Recife.",
                        fontSize = 12.sp,
                        color = textSecondaryColor,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(21.dp),
                        color = accentOrange
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = searchQueryContent,
                                onValueChange = { searchQueryContent = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                decorationBox = { innerTextField ->
                                    if (searchQueryContent.isEmpty()) {
                                        Text("Pesquise aqui...", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            )
                            IconeCanvas(tipo = TipoIcone.BUSCA, tint = Color.White, size = 16.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "FAQ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    faqList.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .shadow(1.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = cardBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(badgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.number,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = textPrimaryColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.question,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = textPrimaryColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.answer,
                                        fontSize = 11.sp,
                                        color = textSecondaryColor,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- BARRA FLUTUANTE (ALINHADA EXATAMENTE COMO A HOME) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            BarraNavegacao(
                onHomeClick = onHomeClick,
                onSearchClick = onSearchClick,
                onItineraryClick = onItineraryClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FaqScreenPreview() {
    FaqScreen()
}
