package com.descubra.recife.data.repository

/**
 * Regras de sincronização dos roteiros entre as duas réplicas:
 * a cópia do aparelho (Room) e a cópia na nuvem (Firestore).
 *
 * Sistemas distribuídos (1): replicação com consistência eventual.
 * Cada réplica pode mudar sozinha (ex.: sem internet). Quando elas se
 * encontram, o conflito é resolvido por "a última escrita vence"
 * (last-write-wins), comparando o horário da última alteração.
 *
 * Fica separado do Room/Firestore para poder ser testado sem Android.
 */
object Sincronizacao {

    /** Versão de um roteiro em uma das réplicas. */
    data class Versao(
        val id: String,
        /** Horário da última alteração (ms). */
        val alteradoEm: Long,
        /** Só na réplica local: mudou no aparelho e ainda não subiu para a nuvem. */
        val pendente: Boolean = false
    )

    data class Plano(
        /** Roteiros do aparelho que precisam subir para a nuvem. */
        val subir: Set<String> = emptySet(),
        /** Roteiros da nuvem que precisam ser gravados no aparelho. */
        val baixar: Set<String> = emptySet(),
        /** Roteiros apagados em outro aparelho, que saem também deste. */
        val apagarLocal: Set<String> = emptySet()
    )

    fun planejar(locais: List<Versao>, remotos: List<Versao>): Plano {
        val local = locais.associateBy { it.id }
        val remoto = remotos.associateBy { it.id }
        val subir = mutableSetOf<String>()
        val baixar = mutableSetOf<String>()
        val apagar = mutableSetOf<String>()

        for (id in local.keys + remoto.keys) {
            val l = local[id]
            val r = remoto[id]
            when {
                // Só existe no aparelho: se foi feito offline, sobe; se já tinha
                // subido antes e sumiu da nuvem, foi apagado em outro aparelho.
                r == null -> if (l!!.pendente) subir += id else apagar += id
                // Só existe na nuvem (ex.: login em um aparelho novo): baixa.
                l == null -> baixar += id
                // Existe nos dois e o aparelho tem mudança nova: a mais recente vence.
                l.pendente && l.alteradoEm >= r.alteradoEm -> subir += id
                // Nos outros casos a nuvem é a versão mais atualizada.
                else -> baixar += id
            }
        }
        return Plano(subir, baixar, apagar)
    }
}
