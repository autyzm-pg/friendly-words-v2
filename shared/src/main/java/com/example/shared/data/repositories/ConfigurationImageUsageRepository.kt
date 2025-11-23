package com.example.shared.data.repositories

import com.example.shared.data.daos.ConfigurationImageUsageDao
import com.example.shared.data.entities.ConfigurationImageUsage
import javax.inject.Inject

class ConfigurationImageUsageRepository @Inject constructor(
    private val dao: ConfigurationImageUsageDao
){
    suspend fun add(usage: ConfigurationImageUsage) = dao.insert(usage)

    suspend fun addMany(usages: List<ConfigurationImageUsage>) { dao.insertMany(usages) }

    suspend fun removeByConfigurationId(configurationId: Long) = dao.deleteByConfigurationId(configurationId)

    suspend fun removeOne(configurationId: Long, imageId: Long) = dao.deleteOne(configurationId, imageId)

    suspend fun getByConfigurationId(configurationId: Long): List<ConfigurationImageUsage> = dao.getByConfigurationId(configurationId)
}