package com.ogos.apprandomizador.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OracleMigrationTest {
    @Test fun migration12PreservesListsAndDrawHistory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "oracle-migration-test.db"
        context.deleteDatabase(name)
        try {
            val file = context.getDatabasePath(name)
            file.parentFile?.mkdirs()
            val old = SQLiteDatabase.openOrCreateDatabase(file, null)
            try {
                old.execSQL("CREATE TABLE IF NOT EXISTS `item_list` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `topic` TEXT NOT NULL, `items` TEXT NOT NULL, `uses` INTEGER NOT NULL, `lastUsedAt` INTEGER, `createdAt` INTEGER NOT NULL)")
                old.execSQL("CREATE TABLE IF NOT EXISTS `draw_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `itemListId` INTEGER NOT NULL, `resultName` TEXT NOT NULL, `drawnAt` INTEGER NOT NULL, FOREIGN KEY(`itemListId`) REFERENCES `item_list`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                old.execSQL("CREATE INDEX IF NOT EXISTS `index_draw_history_itemListId` ON `draw_history` (`itemListId`)")
                old.execSQL("INSERT INTO item_list (id, topic, items, uses, lastUsedAt, createdAt) VALUES (7, 'Lista preservada', '[]', 4, 1000, 1000)")
                old.execSQL("INSERT INTO draw_history (id, itemListId, resultName, drawnAt) VALUES (9, 7, 'Resultado preservado', 1000)")
                old.version = 1
            } finally {
                old.close()
            }

            val migrated = androidx.room.Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()
            try {
                val db = migrated.openHelper.writableDatabase
                assertEquals(1, db.query("SELECT COUNT(*) FROM item_list").use { it.moveToFirst(); it.getInt(0) })
                assertEquals(1, db.query("SELECT COUNT(*) FROM draw_history").use { it.moveToFirst(); it.getInt(0) })
                assertEquals(0, db.query("SELECT COUNT(*) FROM oracle_readings").use { it.moveToFirst(); it.getInt(0) })
            } finally {
                migrated.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
