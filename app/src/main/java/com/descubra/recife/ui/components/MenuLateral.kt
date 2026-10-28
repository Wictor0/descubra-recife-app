package com.descubra.recife.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.descubra.recife.ui.theme.Laranja
import com.descubra.recife.ui.theme.Poppins
import com.descubra.recife.ui.theme.SobreLaranja


/** Menu lateral laranja (protótipo, tela 7), aberto ao tocar no avatar. */
@Composable
fun MenuLateral(
    drawerState: DrawerState,
    nomeCliente: String,
    inicialCliente: String,
    modoEscuro: Boolean,
    onVerPerfil: () -> Unit,
    onCriar: () -> Unit,
    onModoEscuro: (Boolean) -> Unit,
    onFaq: () -> Unit,
    onSair: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Laranja,
                drawerShape = RoundedCornerShape(0.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onVerPerfil)
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarCliente(inicialCliente, 56.dp)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(nomeCliente, fontFamily = Poppins, fontSize = 20.sp, color = SobreLaranja)
                            Text("Ver perfil", fontFamily = Poppins, fontSize = 14.sp, color = SobreLaranja)
                        }
                    }
                    HorizontalDivider(color = SobreLaranja.copy(alpha = 0.6f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))
                    ItemMenu(Icons.Filled.Add, "Criar", onClick = onCriar)
                    ItemMenu(
                        Icons.Filled.DarkMode,
                        "Modo Escuro",
                        subtitulo = if (modoEscuro) "Ativado" else "Desativado",
                        onClick = { onModoEscuro(!modoEscuro) }
                    ) {
                        Switch(
                            checked = modoEscuro,
                            onCheckedChange = onModoEscuro,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Laranja,
                                checkedTrackColor = SobreLaranja,
                                uncheckedTrackColor = Color.Transparent,
                                uncheckedBorderColor = SobreLaranja,
                                uncheckedThumbColor = SobreLaranja
                            )
                        )
                    }
                    ItemMenu(Icons.Filled.QuestionAnswer, "FAQ", onClick = onFaq)
                    Spacer(Modifier.weight(1f))
                    ItemMenu(Icons.AutoMirrored.Filled.Logout, "Sair", onClick = onSair)
                }
            }
        },
        content = content
    )
}

@Composable
private fun ItemMenu(
    icone: ImageVector,
    titulo: String,
    subtitulo: String? = null,
    onClick: () -> Unit,
    extra: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
            Icon(icone, contentDescription = null, tint = SobreLaranja, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = SobreLaranja, maxLines = 1, softWrap = false)
            if (subtitulo != null) Text(subtitulo, fontFamily = Poppins, fontSize = 13.sp, color = SobreLaranja)
        }
        extra?.invoke()
    }
}
