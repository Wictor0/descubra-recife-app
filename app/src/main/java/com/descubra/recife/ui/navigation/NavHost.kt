package com.descubra.recife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.descubra.recife.di.AppContainer
import com.descubra.recife.ui.components.TelaComBarra
import com.descubra.recife.ui.screens.auth.AuthViewModel
import com.descubra.recife.ui.screens.auth.CadastroScreen
import com.descubra.recife.ui.screens.auth.LoginScreen
import com.descubra.recife.ui.screens.busca.BuscaScreen
import com.descubra.recife.ui.screens.busca.BuscaViewModel
import com.descubra.recife.ui.screens.faq.FaqScreen
import com.descubra.recife.ui.screens.home.HomeScreen
import com.descubra.recife.ui.screens.home.HomeViewModel
import com.descubra.recife.ui.screens.local.LocalScreen
import com.descubra.recife.ui.screens.local.LocalViewModel
import com.descubra.recife.ui.screens.perfil.PerfilScreen
import com.descubra.recife.ui.screens.roteiro.RoteiroScreen
import com.descubra.recife.ui.screens.roteiro.RoteiroViewModel

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
    val inicio = if (clienteId == null) Rotas.LOGIN else Rotas.HOME

    // Login/logout trocam a pilha inteira de telas.
    LaunchedEffect(clienteId) {
        val destino = if (clienteId == null) Rotas.LOGIN else Rotas.HOME
        if (nav.currentDestination?.route != destino) {
            nav.navigate(destino) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    val abas = Abas(nav)

    NavHost(navController = nav, startDestination = inicio) {
        composable(Rotas.LOGIN) {
            LoginScreen(
                viewModel = viewModel { AuthViewModel(container.authRepository) },
                modoDemo = !container.usandoFirebase,
                onCadastroClick = { nav.navigate(Rotas.CADASTRO) }
            )
        }
        composable(Rotas.CADASTRO) {
            CadastroScreen(
                viewModel = viewModel { AuthViewModel(container.authRepository) },
                onVoltar = { nav.popBackStack() }
            )
        }
        composable(Rotas.HOME) {
            TelaComBarra(abas::home, abas::busca, abas::roteiro) {
                HomeScreen(
                    viewModel = viewModel { HomeViewModel(container.catalogoRepository) },
                    onLocalClick = { nav.navigate(Rotas.local(it)) },
                    onPerfilClick = { nav.navigate(Rotas.PERFIL) }
                )
            }
        }
        composable(Rotas.BUSCA) {
            TelaComBarra(abas::home, abas::busca, abas::roteiro) {
                BuscaScreen(
                    viewModel = viewModel { BuscaViewModel(container.catalogoRepository) },
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
                        container.rascunhoRoteiro
                    )
                },
                onVoltar = { nav.popBackStack() }
            )
        }
        composable(Rotas.ROTEIRO) {
            TelaComBarra(abas::home, abas::busca, abas::roteiro) {
                RoteiroScreen(
                    viewModel = viewModel {
                        RoteiroViewModel(container.authRepository, container.roteiroRepository, container.rascunhoRoteiro)
                    }
                )
            }
        }
        composable(Rotas.PERFIL) {
            PerfilScreen(
                auth = container.authRepository,
                usandoFirebase = container.usandoFirebase,
                onFaqClick = { nav.navigate(Rotas.FAQ) },
                onVoltar = { nav.popBackStack() }
            )
        }
        composable(Rotas.FAQ) {
            FaqScreen(
                isDarkMode = isDarkMode,
                onHomeClick = abas::home,
                onSearchClick = abas::busca,
                onItineraryClick = abas::roteiro,
                onProfileClick = { nav.navigate(Rotas.PERFIL) },
                onBackClick = { nav.popBackStack() }
            )
        }
    }
}

/** Navegação entre as abas da barra, sem empilhar a mesma aba várias vezes. */
private class Abas(private val nav: NavHostController) {
    fun home() = ir(Rotas.HOME)
    fun busca() = ir(Rotas.BUSCA)
    fun roteiro() = ir(Rotas.ROTEIRO)

    private fun ir(rota: String) = nav.navigate(rota) {
        popUpTo(Rotas.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
