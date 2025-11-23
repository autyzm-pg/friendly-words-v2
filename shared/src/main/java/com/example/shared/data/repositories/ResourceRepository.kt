package com.example.shared.data.repositories

import com.example.shared.data.another.ResourceWithImages
import com.example.shared.data.daos.ImageDao
import com.example.shared.data.daos.ResourceDao
import com.example.shared.data.database.AppDatabase
import com.example.shared.data.entities.Resource
import javax.inject.Inject


class ResourceRepository @Inject constructor(
    private val resourceDao: ResourceDao
) {
    suspend fun insert(resource: Resource) = resourceDao.insert(resource)
    fun getAll() = resourceDao.getAll()
    suspend fun delete(resource: Resource) = resourceDao.delete(resource)
    suspend fun update(resource: Resource) = resourceDao.update(resource)
    suspend fun getById(resourceId: Long) = resourceDao.getById(resourceId)
    suspend fun getAllOnce(): List<Resource> = resourceDao.getAllOnce()
    suspend fun getByName(name: String): Resource {
        return resourceDao.getByName(name) ?: throw IllegalArgumentException("Resource '$name' not found")
    }
    //suspend fun getResourcesWithImages(): List<ResourceWithImages> = resourceDao.getResourcesWithImages()
}