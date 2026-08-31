package com.pawedcat.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pawedcat.app.data.local.dao.AutoDownloadRuleDao
import com.pawedcat.app.data.local.dao.EpisodeDao
import com.pawedcat.app.data.local.dao.PodcastDao
import com.pawedcat.app.data.local.dao.QueueDao
import com.pawedcat.app.data.local.entity.AutoDownloadRuleEntity
import com.pawedcat.app.data.local.entity.EpisodeEntity
import com.pawedcat.app.data.local.entity.PodcastEntity
import com.pawedcat.app.data.local.entity.QueueItemEntity

@Database(
    entities = [
        PodcastEntity::class,
        EpisodeEntity::class,
        QueueItemEntity::class,
        AutoDownloadRuleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PawedCatDatabase : RoomDatabase() {
    abstract fun podcastDao(): PodcastDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun queueDao(): QueueDao
    abstract fun autoDownloadRuleDao(): AutoDownloadRuleDao

    companion object {
        @Volatile
        private var INSTANCE: PawedCatDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE podcasts ADD COLUMN volumeBoostDb INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): PawedCatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PawedCatDatabase::class.java,
                    "pawedcat.db"
                ).addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
