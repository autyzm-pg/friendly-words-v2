package com.example.shared.data.repositories

import com.example.shared.data.daos.ImageDao
import com.example.shared.data.entities.Image
import com.example.shared.data.entities.ResourceImage
import javax.inject.Inject

class ImageRepository @Inject constructor(private val imageDao: ImageDao) {
    suspend fun insert(image: Image) = imageDao.insert(image)
    suspend fun insertMany(images: List<Image>): List<Long> {
        return imageDao.insertMany(images)
    }
    suspend fun delete(image: Image) = imageDao.delete(image)
    suspend fun getAll() = imageDao.getAll()
    suspend fun getByResourceId(resourceId: Long): List<Image> = imageDao.getByResourceId(resourceId)
    suspend fun deleteUnassignedImages() {
        val images = imageDao.getAll()
        for (image in images) {
            val refs = imageDao.getReferenceCount(image.id)
            if (refs == 0) imageDao.delete(image)
        }
    }
}