package com.example.shared.data.repositories

import com.example.shared.data.daos.ConfigurationResourceDao
import com.example.shared.data.entities.ConfigurationResource
import jakarta.inject.Singleton
import javax.inject.Inject

@Singleton
class ConfigurationResourceRepository @Inject constructor(
    private val configurationResourceDao: ConfigurationResourceDao
) {
    suspend fun add(link: ConfigurationResource) = configurationResourceDao.insert(link)

    suspend fun addMany(links: List<ConfigurationResource>) = configurationResourceDao.insertMany(links)

    suspend fun removeOne(configurationId: Long, resourceId: Long) = configurationResourceDao.deleteOne(configurationId,resourceId)

    suspend fun getManyByResourceId(resourceId: Long): List<ConfigurationResource> = configurationResourceDao.getByResourceId(resourceId)

    suspend fun getManyByConfigurationId(configurationId: Long) : List<ConfigurationResource> = configurationResourceDao.getByConfigurationId(configurationId)

    suspend fun removeByConfigurationId(configurationId: Long) = configurationResourceDao.deleteByConfigurationId(configurationId)
}