package com.descubra.recife.data.repository

import com.descubra.recife.data.remote.FirestoreColecoes
import com.descubra.recife.domain.model.Avaliacao
import com.descubra.recife.domain.model.Categoria
import com.descubra.recife.domain.model.Local
import com.descubra.recife.domain.model.Roteiro
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreCatalogoRepository(private val db: FirebaseFirestore) : CatalogoRepository {

    override suspend fun categorias(): List<Categoria> =
        db.collection(FirestoreColecoes.CATEGORIAS).orderBy("nome").get().await()
            .documents.map { it.paraCategoria() }

    override suspend fun locais(categoriaId: String?): List<Local> {
        val colecao = db.collection(FirestoreColecoes.LOCAIS)
        val consulta = if (categoriaId == null) colecao else colecao.whereArrayContains("categoriaIds", categoriaId)
        return consulta.get().await().documents.map { it.paraLocal() }
    }

    override suspend fun local(id: String): Local? {
        val doc = db.collection(FirestoreColecoes.LOCAIS).document(id).get().await()
        return if (doc.exists()) doc.paraLocal() else null
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

    override suspend fun listar(clienteId: String): List<Roteiro> =
        colecao(clienteId).get().await().documents.map { doc ->
            @Suppress("UNCHECKED_CAST")
            val ids = doc.get("localIds") as? List<String> ?: emptyList()
            Roteiro(
                id = doc.id,
                clienteId = clienteId,
                dataGeracao = doc.getLong("dataGeracao") ?: 0L,
                locais = ids.mapNotNull { catalogo.local(it) }
            )
        }
}

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
    imagens = get("imagens") as? List<String> ?: emptyList()
)
