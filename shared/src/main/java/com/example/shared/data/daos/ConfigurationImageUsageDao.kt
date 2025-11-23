package com.example.shared.data.daos

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shared.data.entities.ConfigurationImageUsage

interface ConfigurationImageUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usage: ConfigurationImageUsage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(usages: List<ConfigurationImageUsage>)

    @Query("SELECT * FROM configuration_image_usages WHERE configurationId = :configurationId")
    suspend fun getByConfigurationId(configurationId: Long): List<ConfigurationImageUsage>

    @Query("DELETE FROM configuration_image_usages WHERE configurationId = :configurationId")
    suspend fun deleteByConfigurationId(configurationId: Long)

    @Query("DELETE FROM configuration_image_usages WHERE configurationId = :configurationId AND imageId = :imageId")
    suspend fun deleteOne(configurationId: Long, imageId: Long)
}