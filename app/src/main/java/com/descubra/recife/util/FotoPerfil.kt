package com.descubra.recife.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Foto de perfil guardada no próprio documento do Cliente no Firestore
 * (JPEG pequeno em Base64). Assim ela acompanha a conta em qualquer aparelho,
 * sem precisar do Firebase Storage.
 */
object FotoPerfil {
    private const val LADO_MAXIMO = 320
    private const val QUALIDADE = 80

    /** O que o Coil carrega: a URL da conta Google ou os bytes da foto salva. */
    fun modelo(foto: String?): Any? = when {
        foto.isNullOrBlank() -> null
        foto.startsWith("http") -> foto
        else -> runCatching { Base64.decode(foto, Base64.DEFAULT) }.getOrNull()
    }

    /** Lê a imagem escolhida na galeria, reduz para 320 px e devolve em Base64. */
    fun prepararDaGaleria(context: Context, uri: Uri): String {
        val original = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
                ?: error("Não foi possível abrir a imagem")
        }
        val escala = LADO_MAXIMO.toFloat() / max(original.width, original.height)
        val reduzida = if (escala < 1f) {
            Bitmap.createScaledBitmap(original, (original.width * escala).toInt(), (original.height * escala).toInt(), true)
        } else original
        val bytes = ByteArrayOutputStream().also { reduzida.compress(Bitmap.CompressFormat.JPEG, QUALIDADE, it) }.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
