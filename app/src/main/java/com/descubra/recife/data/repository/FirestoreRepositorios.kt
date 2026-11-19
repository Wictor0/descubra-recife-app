package com.descubra.recife.data.repository

import com.descubra.recife.data.remote.FirestoreColecoes
import com.descubra.recife.domain.model.Avaliacao
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.tasks.await

class FirestoreCatalogoRepository(private val db: FirebaseFirestore) : CatalogoRepository {

    override suspend fun categorias(): List<Categoria> =
        servidorOuCache { db.collection(FirestoreColecoes.CATEGORIAS).get(it).await() }
            .documents.sortedWith(porOrdem).map { it.paraCategoria() }

    override suspend fun locais(categoriaId: String?): List<Local> {
        val colecao = db.collection(FirestoreColecoes.LOCAIS)
        val consulta = if (categoriaId == null) colecao else colecao.whereArrayContains("categoriaIds", categoriaId)
        return servidorOuCache { consulta.get(it).await() }.documents.sortedWith(porOrdem).map { it.paraLocal() }
    }

    override suspend fun local(id: String): Local? {
        val doc = servidorOuCache { db.collection(FirestoreColecoes.LOCAIS).document(id).get(it).await() }
        return if (doc.exists()) doc.paraLocal() else null
    }

    /**
     * Sem internet o Firestore fica tentando o servidor por vários segundos antes
     * de usar o cache. Aqui o app espera no máximo [ESPERA_SERVIDOR_MS] e então
     * mostra a cópia guardada no aparelho.
     */
    private suspend fun <T> servidorOuCache(ler: suspend (Source) -> T): T =
        try {
            withTimeout(ESPERA_SERVIDOR_MS) { ler(Source.SERVER) }
        } catch (e: Exception) {
            ler(Source.CACHE)
        }

    private companion object {
        const val ESPERA_SERVIDOR_MS = 4_000L
    }
}

class FirestoreAvaliacaoRepository(private val db: FirebaseFirestore) : AvaliacaoRepository {

    override suspend fun avaliacoesDoLocal(localId: String): List<Avaliacao> =
        db.collection(FirestoreColecoes.AVALIACOES).whereEqualTo("localId", localId).get().await()
            .documents.map { doc ->
                Avaliacao(
                    id = doc.id,
                    clienteId = doc.getString("clienteId").orEmpty(),
                    localId = localId,
                    nota = doc.getLong("nota")?.toInt() ?: 1,
                    comentario = doc.getString("comentario").orEmpty(),
                    data = doc.getLong("data") ?: 0L
                )
            }

    override suspend fun avaliar(avaliacao: Avaliacao) {
        db.collection(FirestoreColecoes.AVALIACOES).document(avaliacao.id).set(
            mapOf(
                "clienteId" to avaliacao.clienteId,
                "localId" to avaliacao.localId,
                "nota" to avaliacao.nota,
                "comentario" to avaliacao.comentario,
                "data" to avaliacao.data
            )
        ).await()
    }
}

class FirestoreRoteiroRemoto(
    private val db: FirebaseFirestore,
    private val catalogo: CatalogoRepository
) : RoteiroRemoto {

    private fun colecao(clienteId: String) =
        db.collection(FirestoreColecoes.CLIENTES).document(clienteId).collection(FirestoreColecoes.ROTEIROS)

    override suspend fun salvar(roteiro: Roteiro) {
        colecao(roteiro.clienteId).document(roteiro.id).set(
            mapOf(
                "dataGeracao" to roteiro.dataGeracao,
                "localIds" to roteiro.locais.map { it.id }
            )
        ).await()
    }

    override suspend fun excluir(clienteId: String, roteiroId: String) {
        colecao(clienteId).document(roteiroId).delete().await()
    }

    // Source.SERVER: lê do servidor de verdade. Sem internet dá erro, e o app
    // continua com a cópia do aparelho (em vez de uma cópia antiga do cache).
    override suspend fun listar(clienteId: String): List<Roteiro> =
        paraRoteiros(clienteId, colecao(clienteId).get(Source.SERVER).await().documents)

    /**
     * Sistemas distribuídos (2): publish/subscribe. O app se inscreve na coleção
     * de roteiros do Cliente e o Firestore avisa a cada mudança, inclusive as
     * feitas em outro aparelho, sem o app precisar perguntar de novo.
     */
    override fun observar(clienteId: String): Flow<List<Roteiro>> = callbackFlow {
        val inscricao = colecao(clienteId).addSnapshotListener { snapshot, erro ->
            when {
                erro != null -> close(erro)
                // Ignora a cópia do cache e as escritas deste aparelho ainda não confirmadas.
                snapshot == null || snapshot.metadata.isFromCache || snapshot.metadata.hasPendingWrites() -> Unit
                else -> trySend(snapshot.documents)
            }
        }
        awaitClose { inscricao.remove() }
    }.map { documentos -> paraRoteiros(clienteId, documentos) }

    private suspend fun paraRoteiros(clienteId: String, documentos: List<DocumentSnapshot>): List<Roteiro> =
        documentos.map { doc ->
            @Suppress("UNCHECKED_CAST")
            val ids = doc.get("localIds") as? List<String> ?: emptyList()
            Roteiro(
                id = doc.id,
                clienteId = clienteId,
                dataGeracao = doc.getLong("dataGeracao") ?: 0L,
                locais = ids.mapNotNull { runCatching { catalogo.local(it) }.getOrNull() }
            )
        }
}

/** Ordem definida no campo "ordem" (a do protótipo); sem ele, vai para o fim, por id. */
private val porOrdem = compareBy<DocumentSnapshot>({ it.getLong("ordem") ?: Long.MAX_VALUE }, { it.id })

private fun DocumentSnapshot.paraCategoria() = Categoria(
    id = id,
    nome = getString("nome").orEmpty(),
    icone = getString("icone").orEmpty(),
    slug = getString("slug").orEmpty()
)

@Suppress("UNCHECKED_CAST")
private fun DocumentSnapshot.paraLocal() = Local(
    id = id,
    titulo = getString("titulo").orEmpty(),
    descricao = getString("descricao").orEmpty(),
    endereco = getString("endereco").orEmpty(),
    custoAcesso = getString("custoAcesso").orEmpty(),
    horarioFuncionamento = getString("horarioFuncionamento").orEmpty(),
    contato = getString("contato").orEmpty(),
    latitude = getDouble("latitude") ?: 0.0,
    longitude = getDouble("longitude") ?: 0.0,
    categoriaIds = get("categoriaIds") as? List<String> ?: emptyList(),
    imagens = get("imagens") as? List<String> ?: emptyList(),
    destaque = getString("destaque"),
    bairro = getString("bairro").orEmpty()
)
