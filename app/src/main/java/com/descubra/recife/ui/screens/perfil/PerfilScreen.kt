package com.descubra.recife.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.descubra.recife.BuildConfig
import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.domain.model.Cliente

@Composable
fun PerfilScreen(auth: AuthRepository, usandoFirebase: Boolean, onFaqClick: () -> Unit, onVoltar: () -> Unit) {
    var cliente by remember { mutableStateOf<Cliente?>(null) }
    LaunchedEffect(Unit) { cliente = runCatching { auth.clienteAtual() }.getOrNull() }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextButton(onClick = onVoltar) { Text("Voltar") }
        Text("Meu perfil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        cliente?.let {
            Text(it.nome.ifBlank { "Cliente" }, fontWeight = FontWeight.Bold)
            Text(it.email)
            it.telefone?.let { tel -> Text(tel) }
        }
        OutlinedButton(onClick = onFaqClick, modifier = Modifier.fillMaxWidth()) { Text("Ajuda e FAQ") }
        Button(onClick = auth::sair, modifier = Modifier.fillMaxWidth()) { Text("Sair") }
        Text(
            "Versão ${BuildConfig.VERSION_NAME} · ambiente ${BuildConfig.AMBIENTE} · " +
                if (usandoFirebase) "Firebase conectado" else "modo demonstração",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
