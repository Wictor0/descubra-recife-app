package com.descubra.recife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RoteiroEntity::class, RoteiroLocalEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun roteiroDao(): RoteiroDao

    companion object {
        fun criar(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "descubra_recife.db").build()
    }
}
