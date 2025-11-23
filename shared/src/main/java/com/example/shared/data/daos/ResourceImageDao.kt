package com.example.shared.data.daos

import androidx.room.Insert
import androidx.room.Query
import com.example.shared.data.entities.ResourceImage

interface ResourceImageDao {

    @Insert
    suspend fun insertMany(links: List<ResourceImage>)

    @Query("DELETE FROM resource_images WHERE resourceId = :resourceId")
    suspend fun deleteByResourceId(resourceId: Long)

    @Query("DELETE FROM resource_images WHERE resourceId = :resourceId AND imageId IN (:imageIds)")
    suspend fun deleteSpecificImageLinks(resourceId: Long, imageIds: List<Long>)

}