package com.example.shared.data.repositories

import com.example.shared.data.daos.ResourceImageDao
import com.example.shared.data.entities.ResourceImage
import javax.inject.Inject

class ResourceImageRepository @Inject constructor(
    private val dao: ResourceImageDao
){
    suspend fun linkImagesToResource(resourceId: Long, imageIds: List<Long>) {
        dao.deleteByResourceId(resourceId)
        dao.insertMany(
            imageIds.map { imageId -> ResourceImage(resourceId = resourceId, imageId = imageId) }
        )
    }

    suspend fun unlinkImagesFromResource(resourceId: Long, imageIds: List<Long>) {
        dao.deleteSpecificImageLinks(resourceId, imageIds)
    }
}