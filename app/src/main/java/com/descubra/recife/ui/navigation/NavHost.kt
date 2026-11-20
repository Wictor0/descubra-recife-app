package com.descubra.recife.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.descubra.recife.di.AppContainer
import com.descubra.recife.domain.model.Cliente
import com.descubra.recife.ui.components.Aba
import com.descubra.recife.ui.components.MenuLateral
import com.descubra.recife.ui.components.TelaComBarra
import com.descubra.recife.ui.screens.auth.AuthViewModel
import com.descubra.recife.ui.screens.auth.CadastroScreen
import com.descubra.recife.ui.screens.auth.LoginScreen
import com.descubra.recife.ui.screens.faq.FaqScreen
import com.descubra.recife.ui.screens.home.HomeScreen
import com.descubra.recife.ui.screens.home.HomeViewModel
import com.descubra.recife.ui.screens.local.LocalScreen
import com.descubra.recife.ui.screens.local.LocalViewModel
import com.descubra.recife.ui.screens.perfil.PerfilScreen
import com.descubra.recife.ui.screens.roteiro.RoteiroScreen
import com.descubra.recife.ui.screens.roteiro.RoteiroViewModel
import com.descubra.recife.util.FotoPerfil
import kotlinx.coroutines.launch

object Rotas {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"
    const val HOME = "home"
    const val BUSCA = "busca"
    const val LOCAL = "local/{id}"
    const val ROTEIRO = "roteiro"
    const val PERFIL = "perfil"
    const val FAQ = "faq"

    fun local(id: String) = "local/$id"
}

@Composable
fun DescubraRecifeNavHost(container: AppContainer, isDarkMode: Boolean) {
    val nav = rememberNavController()
    val clienteId by container.authRepository.clienteAtualId.collectAsStateWithLifecycle()
    val inicio = remember { if (clienteId == null) Rotas.LOGIN else Rotas.HOME }

    // Recarrega nome/inicial do avatar ao logar e depois de editar o perfil.
    var versaoPerfil by remember { mutableIntStateOf(0) }
    val cliente by produceState<Cliente?>(null, clienteId, versaoPerfil) {
        value = if (clienteId == null) null else runCatching { container.authRepository.clienteAtual() }.getOrNull()
    }
    val inicial = cliente?.inicial ?: "?"
    val foto = remember(cliente?.foto) { FotoPerfil.modelo(cliente?.foto) }

    // Ao entrar na conta (inclusive em outro aparelho), já traz o roteiro salvo na nuvem.
    LaunchedEffect(clienteId) {
        if (clienteId != null) container.roteiroDoCliente.sincronizar()
    }

    // Login/logout trocam a pilha inteira de telas.
    LaunchedEffect(clienteId) {
        val destino = if (clienteId == null) Rotas.LOGIN else Rotas.HOME
        if (nav.currentDestination?.route != destino) {
            nav.navigate(destino) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    val abas = Abas(nav)

    // Telas com banner + barra inferior + menu lateral (abre no avatar).
    @Composable
    fun Principal(aba: Aba, conteudo: @Composable (abrirMenu: () -> Unit) -> Unit) {
        val drawer = rememberDrawerState(DrawerValue.Closed)
        val escopo = rememberCoroutineScope()
        MenuLateral(
            drawerState = drawer,
            nomeCliente = cliente?.nome?.ifBlank { null } ?: "Visitante",
            inicialCliente = inicial,
            fotoCliente = foto,
            modoEscuro = isDarkMode,
            onVerPerfil = { escopo.launch { drawer.close() }; nav.navigate(Rotas.PERFIL) },
            onCriar = { escopo.launch { drawer.close() }; abas.roteiro() },
            onModoEscuro = { container.modoEscuro.value = it },
            onFaq = { escopo.launch { drawer.close() }; nav.navigate(Rotas.FAQ) },
            onSair = { escopo.launch { drawer.close() }; container.authRepository.sair() }
        ) {
            TelaComBarra(abas::home, abas::busca, abas::roteiro, aba) {
                conteudo { escopo.launch { drawer.open() } }
            }
        }
    }

    NavHost(navController = nav, startDestination = inicio) {
        composable(Rotas.LOGIN) {
            LoginScreen(
                viewModel = viewModel { AuthViewModel(container.authRepository) },
                modoDemo = !container.usandoFirebase,
                onCadastroClick = { nav.navigate(Rotas.CADASTRO) },
                onAjudaClick = { nav.navigate(Rotas.FAQ) }
            )
        }
        composable(Rotas.CADASTRO) {
            CadastroScreen(
                viewModel = viewModel { AuthViewModel(container.authRepository) },
                onEntrarClick = { nav.popBackStack() }
            )
        }
        composable(Rotas.HOME) {
            Principal(Aba.INICIO) { abrirMenu ->
                HomeScreen(
                    viewModel = viewModel { HomeViewModel(container.catalogoRepository) },
                    inicialCliente = inicial,
                    fotoCliente = foto,
                    focarBusca = false,
                    onAvatarClick = abrirMenu,
                    onLocalClick = { nav.navigate(Rotas.local(it)) }
                )
            }
        }
        composable(Rotas.BUSCA) {
            Principal(Aba.BUSCAR) { abrirMenu ->
                HomeScreen(
                    viewModel = viewModel { HomeViewModel(container.catalogoRepository) },
                    inicialCliente = inicial,
                    fotoCliente = foto,
                    focarBusca = true,
                    onAvatarClick = abrirMenu,
                    onLocalClick = { nav.navigate(Rotas.local(it)) }
                )
            }
        }
        composable(Rotas.LOCAL) { entrada ->
            val id = entrada.arguments?.getString("id").orEmpty()
            LocalScreen(
                viewModel = viewModel(key = "local-$id") {
                    LocalViewModel(
                        id,
                        container.catalogoRepository,
                        container.avaliacaoRepository,
                        container.authRepository,
                        container.roteiroDoCliente
                    )
                },
                onVoltar = { nav.popBackStack() }
            )
        }
        composable(Rotas.ROTEIRO) {
            Principal(Aba.ROTEIRO) { abrirMenu ->
                RoteiroScreen(
                    viewModel = viewModel {
                        RoteiroViewModel(container.roteiroDoCliente, container.catalogoRepository, container.avaliacaoRepository)
                    },
                    inicialCliente = inicial,
                    fotoCliente = foto,
                    onAvatarClick = abrirMenu,
                    onVoltar = abas::home,
                    onLocalClick = { nav.navigate(Rotas.local(it)) }
                )
            }
        }
        composable(Rotas.PERFIL) {
            PerfilScreen(
                auth = container.authRepository,
                onVoltar = { nav.popBackStack() },
                onPerfilAtualizado = { versaoPerfil++ }
            )
        }
        composable(Rotas.FAQ) {
            val voltar = { nav.popBackStack(); Unit }
            if (clienteId != null) {
                // Logado: igual às outras telas (foto, menu lateral e barra de abas).
                Principal(Aba.NENHUMA) { abrirMenu ->
                    FaqScreen(inicial, foto, onAvatarClick = abrirMenu, onVoltar = voltar)
                }
            } else {
                // Veio do "Precisar de ajuda?" do login: sem conta, sem avatar nem abas.
                FaqScreen(inicial, null, onAvatarClick = null, onVoltar = voltar)
            }
        }
    }
}

/**
 * Navegação entre as abas da barra. A pilha fica sempre "Início" ou
 * "Início > outra aba", então o voltar do Android e o botão Início sempre
 * levam para a tela inicial, e a mesma aba nunca é empilhada duas vezes.
 */
private class Abas(private val nav: NavHostController) {
    fun home() {
        // Volta até o Início que já está na pilha; se não estiver, recria a pilha nele.
        if (!nav.popBackStack(Rotas.HOME, inclusive = false)) {
            nav.navigate(Rotas.HOME) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    fun busca() = ir(Rotas.BUSCA)
    fun roteiro() = ir(Rotas.ROTEIRO)

    private fun ir(rota: String) {
        if (nav.currentDestination?.route == rota) return
        nav.navigate(rota) {
            popUpTo(Rotas.HOME)
            launchSingleTop = true
        }
    }
}
