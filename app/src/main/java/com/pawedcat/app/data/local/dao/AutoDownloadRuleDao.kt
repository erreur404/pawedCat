package com.pawedcat.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pawedcat.app.data.local.entity.AutoDownloadRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoDownloadRuleDao {
    @Query("SELECT * FROM auto_download_rules")
    fun getAllRulesFlow(): Flow<List<AutoDownloadRuleEntity>>

    @Query("SELECT * FROM auto_download_rules WHERE isEnabled = 1")
    suspend fun getActiveRules(): List<AutoDownloadRuleEntity>

    @Query("SELECT * FROM auto_download_rules WHERE podcastId = :podcastId LIMIT 1")
    suspend fun getRuleForPodcast(podcastId: Long): AutoDownloadRuleEntity?

    @Query("SELECT * FROM auto_download_rules WHERE podcastId = :podcastId LIMIT 1")
    fun getRuleForPodcastFlow(podcastId: Long): Flow<AutoDownloadRuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRule(rule: AutoDownloadRuleEntity)

    @Update
    suspend fun updateRule(rule: AutoDownloadRuleEntity)

    @Delete
    suspend fun deleteRule(rule: AutoDownloadRuleEntity)

    @Query("DELETE FROM auto_download_rules WHERE podcastId = :podcastId")
    suspend fun deleteRuleForPodcast(podcastId: Long)
}
