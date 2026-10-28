package com.descubra.recife.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.descubra.recife.R
import com.descubra.recife.ui.theme.CinzaBotao
import com.descubra.recife.ui.theme.CinzaCampo
import com.descubra.recife.ui.theme.Laranja
import com.descubra.recife.ui.theme.Pessego
import com.descubra.recife.ui.theme.PessegoClaro
import com.descubra.recife.ui.theme.Poppins

private val TextoEscuro = Color(0xFF2B2B2B)

// ---------- Login (protótipo, tela 3) ----------

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    modoDemo: Boolean,
    onCadastroClick: () -> Unit,
    onAjudaClick: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    val emBreve = { Toast.makeText(context, "Em breve!", Toast.LENGTH_SHORT).show() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Box(Modifier.fillMaxWidth().height(260.dp)) {
            Image(
                painterResource(R.drawable.bg_banner_recife),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Image(
                painterResource(R.drawable.logo_mascara),
                contentDescription = "Descubra Recife",
                modifier = Modifier
                    .align(Alignment.Center)
                    .statusBarsPadding()
                    .size(84.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp)
                .background(Color(0xFFFAFAFA), RoundedCornerShape(topStart = 40.dp))
                .padding(horizontal = 36.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))
            Text("Entrar", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 56.sp, color = TextoEscuro)
            Text("Bem-vindo!", fontFamily = Poppins, fontSize = 15.sp, color = TextoEscuro)
            Spacer(Modifier.height(32.dp))
            Text(
                "Entrar no Descubra Recife",
                fontFamily = Poppins,
                fontSize = 15.sp,
                color = TextoEscuro,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            CampoCinza("Email", email, { email = it; viewModel.limparErro() }, teclado = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))
            CampoCinza("Senha", senha, { senha = it; viewModel.limparErro() }, senha = true)
            Spacer(Modifier.height(16.dp))
            BotaoRetangular(
                texto = if (estado.carregando) "Entrando..." else "Continue",
                cor = if (email.isNotBlank() && senha.isNotBlank()) Laranja else CinzaBotao,
                corTexto = if (email.isNotBlank() && senha.isNotBlank()) Color.White else TextoEscuro,
                onClick = { viewModel.entrar(email, senha) },
                habilitado = !estado.carregando
            )
            estado.erro?.let {
                Text(it, color = Color(0xFFC62828), fontFamily = Poppins, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Precisar de ajuda?", fontFamily = Poppins, fontSize = 14.sp, color = TextoEscuro, modifier = Modifier.clickable(onClick = onAjudaClick))
                Text("Entrar de outra forma", fontFamily = Poppins, fontSize = 14.sp, color = TextoEscuro, modifier = Modifier.clickable(onClick = emBreve))
            }
            Spacer(Modifier.height(44.dp))
            BotaoRetangular("Não tem uma conta? Cadastre-se", Laranja, Color.White, onCadastroClick)
            Spacer(Modifier.height(10.dp))
            BotaoRetangular("Entrar com a conta Google", CinzaCampo, TextoEscuro, emBreve, icone = {
                Text("G", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF4285F4))
            })
            if (modoDemo) {
                Text(
                    "Modo demonstração: Firebase não configurado. Qualquer e-mail e senha com 6+ caracteres entra.",
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CampoCinza(
    rotulo: String,
    valor: String,
    onChange: (String) -> Unit,
    senha: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text
) {
    Surface(color = CinzaCampo, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(58.dp)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(rotulo, fontFamily = Poppins, fontSize = 14.sp, color = TextoEscuro)
            BasicTextField(
                value = valor,
                onValueChange = onChange,
                singleLine = true,
                visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = if (senha) KeyboardType.Password else teclado),
                textStyle = TextStyle(fontFamily = Poppins, fontSize = 15.sp, color = TextoEscuro),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BotaoRetangular(
    texto: String,
    cor: Color,
    corTexto: Color,
    onClick: () -> Unit,
    habilitado: Boolean = true,
    icone: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        enabled = habilitado,
        color = cor,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().height(46.dp)
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (icone != null) {
                icone()
                Spacer(Modifier.width(24.dp))
            }
            Text(texto, fontFamily = Poppins, fontSize = 15.sp, color = corTexto)
        }
    }
}

// ---------- Cadastro (protótipo, tela 4) ----------

private val Generos = listOf("Feminino", "Masculino", "Não binário", "Prefiro não dizer")

@Composable
fun CadastroScreen(viewModel: AuthViewModel, onEntrarClick: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf(FormCadastro()) }
    fun muda(novo: FormCadastro) {
        form = novo
        viewModel.limparErro()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Pessego)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            "Começar no Descubra Recife",
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            lineHeight = 38.sp,
            color = TextoEscuro
        )
        Text(
            "Crie sua conta para descobrir o melhor de Recife, montar seu roteiro personalizado e compartilhar passeios inesquecíveis com seus amigos.",
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = TextoEscuro,
            modifier = Modifier.padding(top = 6.dp)
        )

        Rotulo("Nome de usuário")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CampoClaro("Nome próprio", form.nomeProprio, { muda(form.copy(nomeProprio = it)) }, Modifier.weight(1f))
            CampoClaro("Sobrenome", form.apelido, { muda(form.copy(apelido = it)) }, Modifier.weight(1f))
        }

        Rotulo("Data de nascimento")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampoClaro("Dia", form.dia, { muda(form.copy(dia = it.filter(Char::isDigit).take(2))) }, Modifier.weight(1f), KeyboardType.Number)
            CampoClaro("Mês", form.mes, { muda(form.copy(mes = it.filter(Char::isDigit).take(2))) }, Modifier.weight(1f), KeyboardType.Number)
            CampoClaro("Ano", form.ano, { muda(form.copy(ano = it.filter(Char::isDigit).take(4))) }, Modifier.weight(1f), KeyboardType.Number)
        }

        Rotulo("Gênero")
        var abrirGenero by remember { mutableStateOf(false) }
        Box {
            CaixaClara(Modifier.fillMaxWidth().clickable { abrirGenero = true }) {
                Text("Selecionar seu gênero", fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoEscuro)
                Text(form.genero, fontFamily = Poppins, fontSize = 15.sp, color = TextoEscuro)
            }
            DropdownMenu(expanded = abrirGenero, onDismissRequest = { abrirGenero = false }) {
                Generos.forEach { g ->
                    DropdownMenuItem(text = { Text(g, fontFamily = Poppins) }, onClick = {
                        muda(form.copy(genero = g))
                        abrirGenero = false
                    })
                }
            }
        }

        Rotulo("E-mail")
        CampoClaro("nome@domínio.com", form.email, { muda(form.copy(email = it.trim())) }, Modifier.fillMaxWidth(), KeyboardType.Email)

        Rotulo("Senha")
        CampoClaro("Senha", form.senha, { muda(form.copy(senha = it)) }, Modifier.fillMaxWidth(), senha = true)
        Spacer(Modifier.height(12.dp))
        CampoClaro("Confirmar", form.confirmar, { muda(form.copy(confirmar = it)) }, Modifier.fillMaxWidth(), senha = true)
        Text(
            "A senha deve ter pelo menos ${AuthViewModel.SENHA_MINIMA} caracteres.",
            fontFamily = Poppins,
            fontSize = 13.sp,
            color = TextoEscuro,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp)
        )

        Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = form.aceitouTermos,
                onCheckedChange = { muda(form.copy(aceitouTermos = it)) },
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF34C759))
            )
            Spacer(Modifier.width(10.dp))
            Text("Aceito os termos e a política de privacidade.", fontFamily = Poppins, fontSize = 13.sp, color = TextoEscuro)
        }

        estado.erro?.let {
            Text(it, color = Color(0xFFC62828), fontFamily = Poppins, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
        }

        Surface(
            onClick = { viewModel.cadastrar(form) },
            enabled = !estado.carregando,
            color = Color(0xFFB5987A),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .height(50.dp)
                .shadow(4.dp, RoundedCornerShape(10.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (estado.carregando) "Criando conta..." else "Conecte-se",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextoEscuro
                )
            }
        }
        Text(
            "Já tem uma conta? Entre",
            fontFamily = Poppins,
            fontSize = 14.sp,
            color = TextoEscuro,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp)
                .clickable(onClick = onEntrarClick)
        )
    }
}

@Composable
private fun Rotulo(texto: String) {
    Text(
        texto,
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = TextoEscuro,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun CaixaClara(modifier: Modifier, content: @Composable () -> Unit) {
    Surface(
        color = PessegoClaro,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(58.dp).shadow(3.dp, RoundedCornerShape(12.dp))
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) { content() }
    }
}

@Composable
private fun CampoClaro(
    rotulo: String,
    valor: String,
    onChange: (String) -> Unit,
    modifier: Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    senha: Boolean = false
) {
    var visivel by remember { mutableStateOf(false) }
    CaixaClara(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(rotulo, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextoEscuro, maxLines = 1)
                BasicTextField(
                    value = valor,
                    onValueChange = onChange,
                    singleLine = true,
                    visualTransformation = if (senha && !visivel) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = if (senha) KeyboardType.Password else teclado),
                    textStyle = TextStyle(fontFamily = Poppins, fontSize = 15.sp, color = TextoEscuro),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (senha) {
                Icon(
                    if (visivel) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visivel) "Ocultar senha" else "Mostrar senha",
                    tint = Color.Gray,
                    modifier = Modifier.size(22.dp).clickable { visivel = !visivel }
                )
            }
        }
    }
}
