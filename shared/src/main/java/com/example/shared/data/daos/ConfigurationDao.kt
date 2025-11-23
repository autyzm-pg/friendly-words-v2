package com.example.shared.data.daos

import androidx.room.*
import com.example.shared.data.entities.Configuration
import com.example.shared.data.entities.ConfigurationImageUsage
import com.example.shared.data.entities.ConfigurationResource
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigurationDao {

    // crudy dla konfiguracji
    @Query("SELECT * FROM configurations")
    fun getAll(): Flow<List<Configuration>>

    @Query("SELECT * FROM configurations WHERE id = :id")
    suspend fun getById(id: Long): Configuration

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(configuration: Configuration): Long

    @Query("SELECT * FROM configurations")
    suspend fun getAllOnce(): List<Configuration>

    @Delete
    suspend fun delete(configuration: Configuration)

    @Query("SELECT * FROM configurations WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<Configuration>

    @Update
    suspend fun update(configuration: Configuration)

    @Query("UPDATE configurations SET isActive = 0, activeMode = NULL WHERE isActive = 1")
    suspend fun clearActiveConfiguration()

    @Query("UPDATE configurations SET isActive = 1, activeMode = :mode WHERE id = :id")
    suspend fun activateConfiguration(id: Long, mode: String)

    @Query("SELECT * FROM configurations WHERE isActive = 1")
    fun getActiveConfiguration(): Flow<Configuration?>
}
