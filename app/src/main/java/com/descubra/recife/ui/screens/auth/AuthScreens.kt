package com.descubra.recife.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(viewModel: AuthViewModel, modoDemo: Boolean, onCadastroClick: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }

    FormularioAuth(titulo = "Descubra Recife", subtitulo = "Entre para montar seus roteiros") {
        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("E-mail") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = senha, onValueChange = { senha = it }, label = { Text("Senha") },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        estado.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { viewModel.entrar(email, senha) },
            enabled = !estado.carregando,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (estado.carregando) "Entrando..." else "Entrar") }
        TextButton(onClick = onCadastroClick) { Text("Ainda não tenho conta") }
        if (modoDemo) {
            Text(
                "Modo demonstração: Firebase não configurado. Qualquer e-mail e senha com 6+ caracteres entra.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CadastroScreen(viewModel: AuthViewModel, onVoltar: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var senha by rememberSaveable { mutableStateOf("") }
    var nascimento by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }

    FormularioAuth(titulo = "Criar conta", subtitulo = "Seus dados ficam protegidos pela sua conta") {
        OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            email, { email = it }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            senha, { senha = it }, label = { Text("Senha (mín. 6)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(nascimento, { nascimento = it }, label = { Text("Data de nascimento") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            telefone, { telefone = it }, label = { Text("Telefone") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        estado.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { viewModel.cadastrar(nome, email, senha, nascimento, telefone) },
            enabled = !estado.carregando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Cadastrar") }
        TextButton(onClick = onVoltar) { Text("Já tenho conta") }
    }
}

@Composable
private fun FormularioAuth(titulo: String, subtitulo: String, campos: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant)
        campos()
    }
}
