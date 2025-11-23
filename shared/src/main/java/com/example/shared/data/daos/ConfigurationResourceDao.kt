package com.example.shared.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shared.data.entities.ConfigurationResource

@Dao
interface ConfigurationResourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(link: ConfigurationResource)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(resources: List<ConfigurationResource>)

    @Query("SELECT * FROM configuration_resources WHERE resourceId = :resourceId")
    suspend fun getByResourceId(resourceId: Long): List<ConfigurationResource>

    @Query("SELECT * FROM configuration_resources WHERE configurationId = :configurationId")
    suspend fun getByConfigurationId(configurationId: Long): List<ConfigurationResource>

    @Query("DELETE FROM configuration_resources WHERE configurationId = :configurationId AND resourceId = :resourceId")
    suspend fun deleteOne(configurationId: Long, resourceId: Long)

    @Query("DELETE FROM configuration_resources WHERE configurationId = :configId")
    suspend fun deleteByConfigurationId(configId: Long)


}
