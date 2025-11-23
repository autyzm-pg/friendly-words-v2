package com.example.shared.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.shared.data.daos.ConfigurationDao
import com.example.shared.data.daos.ConfigurationImageUsageDao
import com.example.shared.data.daos.ConfigurationResourceDao
import com.example.shared.data.daos.ImageDao
import com.example.shared.data.daos.ResourceDao
import com.example.shared.data.daos.ResourceImageDao
import com.example.shared.data.entities.Resource
import com.example.shared.data.repositories.ConfigurationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "friendly_words"
        )
            .fallbackToDestructiveMigration(true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys=ON;") // to umozliwia dzialanie foreign keys
                }
            })
            .build()

    @Provides
    fun provideResourceDao(db: AppDatabase): ResourceDao = db.resourceDao()

    @Provides
    fun provideImageDao(db: AppDatabase): ImageDao = db.imageDao()

    @Provides
    fun provideConfigurationDao(db: AppDatabase): ConfigurationDao =
        db.configurationDao()
    @Provides
    fun provideConfigurationResourceDao(db: AppDatabase): ConfigurationResourceDao =
        db.configurationResourceDao()
    @Provides
    fun provideConfigurationImageUsageDao(db: AppDatabase): ConfigurationImageUsageDao =
        db.configurationImageUsageDao()
    @Provides
    fun provideResourceImageDao(db: AppDatabase): ResourceImageDao =
        db.resourceImageDao()

    @Provides
    fun provideConfigurationRepository(
        dao: ConfigurationDao,
        resourceDao: ResourceDao,
        configurationResourceDao: ConfigurationResourceDao
    ): ConfigurationRepository =
        ConfigurationRepository(dao, resourceDao, configurationResourceDao)

}
