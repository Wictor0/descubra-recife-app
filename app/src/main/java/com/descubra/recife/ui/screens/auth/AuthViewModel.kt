package com.descubra.recife.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.descubra.recife.data.repository.AuthRepository
import com.descubra.recife.util.LoginGoogle
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(val carregando: Boolean = false, val erro: String? = null)

/** Dados do formulário de cadastro (tela "Começar no Descubra Recife"). */
data class FormCadastro(
    val nomeProprio: String = "",
    val apelido: String = "",
    val dia: String = "",
    val mes: String = "",
    val ano: String = "",
    val genero: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmar: String = "",
    val aceitouTermos: Boolean = false
)

class AuthViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado

    fun entrar(email: String, senha: String) {
        if (email.isBlank() || senha.isBlank()) {
            _estado.value = AuthUiState(erro = "Preencha e-mail e senha")
            return
        }
        executar { auth.entrar(email.trim(), senha) }
    }

    /** O seletor de contas roda na tela (precisa da Activity); aqui só trocamos o token pelo login. */
    fun entrarComGoogle(obterToken: suspend () -> Result<String>) {
        viewModelScope.launch {
            _estado.value = AuthUiState(carregando = true)
            val token = obterToken()
            val erro = token.exceptionOrNull()
            _estado.value = when {
                erro is LoginGoogle.Cancelado -> AuthUiState()
                erro != null -> AuthUiState(erro = mensagem(erro))
                else -> AuthUiState(erro = auth.entrarComGoogle(token.getOrThrow()).exceptionOrNull()?.let(::mensagem))
            }
        }
    }

    fun cadastrar(form: FormCadastro) {
        val erro = validarCadastro(form)
        if (erro != null) {
            _estado.value = AuthUiState(erro = erro)
            return
        }
        val nome = listOf(form.nomeProprio, form.apelido).filter { it.isNotBlank() }.joinToString(" ") { it.trim() }
        executar {
            auth.cadastrar(nome, form.email.trim(), form.senha, dataNascimento(form), form.genero.ifBlank { null })
        }
    }

    fun limparErro() {
        _estado.value = _estado.value.copy(erro = null)
    }

    private fun executar(acao: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _estado.value = AuthUiState(carregando = true)
            val resultado = acao()
            _estado.value = AuthUiState(erro = resultado.exceptionOrNull()?.let(::mensagem))
        }
    }

    companion object {
        const val SENHA_MINIMA = 8

        /** Regras do formulário de cadastro; devolve a mensagem de erro ou null. */
        fun validarCadastro(form: FormCadastro): String? = when {
            form.nomeProprio.isBlank() -> "Informe seu nome"
            !form.email.contains('@') -> "Informe um e-mail válido"
            form.senha.length < SENHA_MINIMA -> "A senha deve ter pelo menos $SENHA_MINIMA caracteres"
            form.senha != form.confirmar -> "As senhas não conferem"
            listOf(form.dia, form.mes, form.ano).any { it.isNotBlank() } && dataNascimento(form) == null ->
                "Data de nascimento inválida"
            !form.aceitouTermos -> "Aceite os termos e a política de privacidade"
            else -> null
        }

        /** "dd/MM/aaaa", ou null se a data não foi preenchida ou é inválida. */
        fun dataNascimento(form: FormCadastro): String? {
            val dia = form.dia.toIntOrNull() ?: return null
            val mes = form.mes.toIntOrNull() ?: return null
            val ano = form.ano.toIntOrNull() ?: return null
            if (dia !in 1..31 || mes !in 1..12 || ano !in 1900..2026) return null
            return "%02d/%02d/%04d".format(dia, mes, ano)
        }

        private fun mensagem(e: Throwable): String = when (e) {
            is FirebaseAuthUserCollisionException -> "Já existe uma conta com esse e-mail"
            is FirebaseAuthWeakPasswordException -> "Senha fraca: use pelo menos $SENHA_MINIMA caracteres"
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos"
            else -> e.message ?: "Algo deu errado. Tente de novo."
        }
    }
}
