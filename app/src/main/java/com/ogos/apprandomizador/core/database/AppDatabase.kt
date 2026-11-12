package com.ogos.apprandomizador.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ogos.apprandomizador.core.database.dao.DrawHistoryDao
import com.ogos.apprandomizador.core.database.dao.ItemDao
import com.ogos.apprandomizador.core.database.dao.OracleDao
import com.ogos.apprandomizador.core.model.DrawHistoryEntry
import com.ogos.apprandomizador.core.model.ItemList

@Database(
    entities = [ItemList::class, DrawHistoryEntry::class, OracleReadingEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun drawHistoryDao(): DrawHistoryDao
    abstract fun oracleDao(): OracleDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `oracle_readings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `modeId` TEXT NOT NULL, `outcomeJson` TEXT NOT NULL, `drawnAt` INTEGER NOT NULL)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_oracle_readings_modeId_drawnAt` ON `oracle_readings` (`modeId`, `drawnAt`)")
            }
        }

        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { INSTANCE = it }
        }
    }
}
