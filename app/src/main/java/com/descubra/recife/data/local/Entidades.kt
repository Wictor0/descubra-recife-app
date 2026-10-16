package com.descubra.recife.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Cópia local dos Roteiros (RA-004 / ADR-003): guarda também os dados
// básicos de cada local para o roteiro abrir mesmo sem internet.

@Entity(tableName = "roteiros", primaryKeys = ["id"])
data class RoteiroEntity(
    val id: String,
    val clienteId: String,
    val dataGeracao: Long,
    val sincronizado: Boolean = false
)

@Entity(
    tableName = "roteiro_locais",
    primaryKeys = ["roteiroId", "localId"],
    foreignKeys = [
        ForeignKey(
            entity = RoteiroEntity::class,
            parentColumns = ["id"],
            childColumns = ["roteiroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roteiroId")]
)
data class RoteiroLocalEntity(
    val roteiroId: String,
    val localId: String,
    val ordem: Int,
    val titulo: String,
    val endereco: String,
    val latitude: Double,
    val longitude: Double
)
