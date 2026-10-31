package com.descubra.recife.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Abre o seletor de contas Google do Android (Credential Manager) e devolve o
 * ID token que o Firebase Authentication usa para logar.
 */
object LoginGoogle {

    private const val TAG = "LoginGoogle"

    /** O usuário fechou o seletor: não é erro, a tela só não faz nada. */
    class Cancelado : Exception()

    /**
     * Client ID "web" do projeto Firebase. O plugin google-services gera a string
     * default_web_client_id a partir do google-services.json; procuramos pelo nome
     * para o app compilar mesmo antes do Google estar ativado no Firebase.
     */
    fun clientIdWeb(context: Context): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) null else context.getString(id)
    }

    suspend fun obterIdToken(context: Context): Result<String> {
        val clientId = clientIdWeb(context)
            ?: return Result.failure(
                IllegalStateException("Login com Google ainda não configurado: ative o Google no Firebase e baixe o google-services.json de novo.")
            )
        val pedido = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()
        return try {
            val credencial = CredentialManager.create(context).getCredential(context, pedido).credential
            if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Result.success(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
            } else {
                Result.failure(IllegalStateException("Resposta inesperada do login com Google"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Cancelado())
        } catch (e: NoCredentialException) {
            Result.failure(IllegalStateException("Nenhuma conta Google neste aparelho. Adicione uma em Configurações > Contas."))
        } catch (e: GetCredentialException) {
            // Ex.: o pedido foi interrompido enquanto o usuário adicionava uma conta no Android
            Log.w(TAG, "Login com Google falhou: ${e.type}", e)
            Result.failure(IllegalStateException("Não foi possível concluir o login com Google. Toque em \"Entrar com a conta Google\" de novo."))
        } catch (e: Exception) {
            Log.w(TAG, "Login com Google falhou", e)
            Result.failure(IllegalStateException("Não foi possível entrar com o Google. Verifique a internet e tente de novo."))
        }
    }
}
