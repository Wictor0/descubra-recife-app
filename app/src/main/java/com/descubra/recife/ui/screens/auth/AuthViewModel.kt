package com.descubra.recife.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(val carregando: Boolean = false, val erro: String? = null)

class AuthViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado

    fun entrar(email: String, senha: String) = executar { auth.entrar(email.trim(), senha) }

    fun cadastrar(nome: String, email: String, senha: String, dataNascimento: String, telefone: String) {
        if (nome.isBlank()) {
            _estado.value = AuthUiState(erro = "Informe seu nome")
            return
        }
        executar {
            auth.cadastrar(
                nome.trim(),
                email.trim(),
                senha,
                dataNascimento.ifBlank { null },
                telefone.ifBlank { null }
            )
        }
    }

    private fun executar(acao: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _estado.value = AuthUiState(carregando = true)
            val resultado = acao()
            _estado.value = AuthUiState(erro = resultado.exceptionOrNull()?.message)
        }
    }
}
