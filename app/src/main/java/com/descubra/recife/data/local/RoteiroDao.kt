package com.descubra.recife.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RoteiroDao {

    @Query("SELECT * FROM roteiros WHERE clienteId = :clienteId ORDER BY dataGeracao DESC")
    fun roteirosDoCliente(clienteId: String): Flow<List<RoteiroEntity>>

    @Query("SELECT * FROM roteiro_locais WHERE roteiroId = :roteiroId ORDER BY ordem")
    fun locaisDoRoteiro(roteiroId: String): Flow<List<RoteiroLocalEntity>>

    @Query("SELECT * FROM roteiro_locais WHERE roteiroId IN (SELECT id FROM roteiros WHERE clienteId = :clienteId) ORDER BY ordem")
    fun locaisDosRoteiros(clienteId: String): Flow<List<RoteiroLocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRoteiro(roteiro: RoteiroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirLocais(locais: List<RoteiroLocalEntity>)

    @Query("DELETE FROM roteiro_locais WHERE roteiroId = :roteiroId")
    suspend fun apagarLocais(roteiroId: String)

    @Query("DELETE FROM roteiros WHERE id = :roteiroId")
    suspend fun apagarRoteiro(roteiroId: String)

    @Query("UPDATE roteiros SET sincronizado = 1 WHERE id = :roteiroId")
    suspend fun marcarSincronizado(roteiroId: String)

    @Transaction
    suspend fun salvar(roteiro: RoteiroEntity, locais: List<RoteiroLocalEntity>) {
        inserirRoteiro(roteiro)
        apagarLocais(roteiro.id)
        inserirLocais(locais)
    }
}
