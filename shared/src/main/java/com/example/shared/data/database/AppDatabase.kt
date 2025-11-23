package com.example.shared.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shared.data.another.Converters
import com.example.shared.data.daos.ConfigurationDao
import com.example.shared.data.daos.ConfigurationImageUsageDao
import com.example.shared.data.daos.ConfigurationResourceDao
import com.example.shared.data.daos.ImageDao
import com.example.shared.data.daos.ResourceDao
import com.example.shared.data.daos.ResourceImageDao
import com.example.shared.data.entities.Configuration
import com.example.shared.data.entities.ConfigurationImageUsage
import com.example.shared.data.entities.ConfigurationResource
import com.example.shared.data.entities.Image
import com.example.shared.data.entities.Resource
import com.example.shared.data.entities.ResourceImage


@Database(
    entities = [
        Resource::class,
        Image::class,
        Configuration::class,
        ConfigurationResource::class,
        ConfigurationImageUsage::class,
        ResourceImage::class
    ],
    version = 28,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun resourceDao(): ResourceDao
    abstract fun imageDao() : ImageDao
    abstract fun configurationDao(): ConfigurationDao
    abstract fun configurationResourceDao(): ConfigurationResourceDao
    abstract fun configurationImageUsageDao(): ConfigurationImageUsageDao
    abstract fun resourceImageDao(): ResourceImageDao
}