package com.descubra.recife.ui.screens.perfil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.domain.model.Cliente
import com.descubra.recife.ui.components.AvatarCliente
import com.descubra.recife.ui.components.BotaoPilula
import com.descubra.recife.ui.theme.Coral
import com.descubra.recife.ui.theme.Poppins
import kotlinx.coroutines.launch

/** Editar perfil (protótipo, tela 8). */
@Composable
fun PerfilScreen(auth: AuthRepository, onVoltar: () -> Unit, onPerfilAtualizado: () -> Unit) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var cliente by remember { mutableStateOf<Cliente?>(null) }
    var nome by remember { mutableStateOf("") }
    var nascimento by remember { mutableStateOf("") }
    var salvando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cliente = runCatching { auth.clienteAtual() }.getOrNull()
        nome = cliente?.nome.orEmpty()
        nascimento = cliente?.dataNascimento.orEmpty()
    }
    fun aviso(texto: String) = Toast.makeText(context, texto, Toast.LENGTH_LONG).show()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterStart).size(26.dp).clickable(onClick = onVoltar)
            )
            Text(
                "Editar perfil",
                fontFamily = Poppins,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(Modifier.height(36.dp))
        Box(Modifier.align(Alignment.CenterHorizontally)) {
            AvatarCliente(Cliente("", nome, "").inicial, 136.dp)
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 4.dp, y = 4.dp)
                    .size(46.dp)
                    .background(Coral, CircleShape)
                    .clickable { aviso("Foto de perfil: em breve!") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Trocar foto", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(48.dp))

        Linha(Icons.Outlined.Person) { CampoLinha(nome, { nome = it }, "Seu nome") }
        Linha(Icons.Outlined.Email) {
            Text(cliente?.email.orEmpty(), fontFamily = Poppins, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Linha(Icons.Outlined.CalendarMonth) {
            CampoLinha(nascimento, { nascimento = it.filter { c -> c.isDigit() || c == '/' }.take(10) }, "Data de nascimento (dd/mm/aaaa)", KeyboardType.Number)
        }
        Linha(Icons.Outlined.VisibilityOff, onClick = {
            escopo.launch {
                auth.redefinirSenha()
                    .onSuccess { aviso("Enviamos um e-mail para você trocar a senha.") }
                    .onFailure { aviso(it.message ?: "Não foi possível enviar o e-mail") }
            }
        }) {
            Text("**********", fontFamily = Poppins, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(Modifier.height(56.dp))
        BotaoPilula(
            texto = if (salvando) "Salvando..." else "Atualizar perfil",
            fontSize = 22,
            habilitado = !salvando && nome.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(64.dp),
            onClick = {
                salvando = true
                escopo.launch {
                    auth.atualizarPerfil(nome.trim(), nascimento.ifBlank { null })
                        .onSuccess { aviso("Perfil atualizado!"); onPerfilAtualizado() }
                        .onFailure { aviso(it.message ?: "Não foi possível salvar") }
                    salvando = false
                }
            }
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Linha(icone: ImageVector, onClick: (() -> Unit)? = null, conteudo: @Composable () -> Unit) {
    Column(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.weight(1f)) { conteudo() }
            Icon(icone, contentDescription = null, tint = Color(0xFFB0B0B0), modifier = Modifier.size(30.dp))
        }
        HorizontalDivider(color = Color(0xFFDDDDDD))
    }
}

@Composable
private fun CampoLinha(valor: String, onChange: (String) -> Unit, dica: String, teclado: KeyboardType = KeyboardType.Text) {
    Box {
        if (valor.isEmpty()) Text(dica, fontFamily = Poppins, fontSize = 16.sp, color = Color(0xFFB0B0B0))
        BasicTextField(
            value = valor,
            onValueChange = onChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            textStyle = TextStyle(fontFamily = Poppins, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
