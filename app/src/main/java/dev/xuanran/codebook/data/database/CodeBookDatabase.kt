package dev.xuanran.codebook.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.xuanran.codebook.data.dao.AppDao
import dev.xuanran.codebook.data.dao.TotpDao
import dev.xuanran.codebook.model.App
import dev.xuanran.codebook.model.Totp

@Database(
    entities = [App::class, Totp::class],
    version = 1,
    exportSchema = false
)
abstract class CodeBookDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun totpDao(): TotpDao

    companion object {
        @Volatile
        private var INSTANCE: CodeBookDatabase? = null

        fun getDatabase(context: Context): CodeBookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CodeBookDatabase::class.java,
                    "codebook_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 