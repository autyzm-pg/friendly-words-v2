package com.example.shared.data.repositories

import com.example.shared.data.another.ResourceWithImages
import com.example.shared.data.daos.ConfigurationDao
import com.example.shared.data.daos.ResourceDao
import com.example.shared.data.entities.Configuration
import com.example.shared.data.entities.ConfigurationImageUsage
import com.example.shared.data.entities.ConfigurationResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import com.example.shared.data.another.ConfigurationMaterialState
import com.example.shared.data.another.VocabularyItem
import com.example.shared.data.another.ConfigurationReinforcementState
import com.example.shared.data.daos.ConfigurationResourceDao

@Singleton
class ConfigurationRepository @Inject constructor(
    private val configurationDao: ConfigurationDao,
    private val configurationResourceRepo: ConfigurationResourceRepository,
    private val configurationImageUsageRepo: ConfigurationImageUsageRepository,
    private val resourceRepo: ResourceRepository
) {
    fun getAll(): Flow<List<Configuration>> = configurationDao.getAll()
    suspend fun getAllOnce(): List<Configuration> = configurationDao.getAllOnce()

    suspend fun getById(id: Long): Configuration = configurationDao.getById(id)

    suspend fun insert(configuration: Configuration): Long = configurationDao.insert(configuration)

    suspend fun delete(configuration: Configuration) = configurationDao.delete(configuration)

    suspend fun update(configuration: Configuration) = configurationDao.update(configuration)

    suspend fun setActiveConfiguration(configuration: Configuration, mode: String) {
        configurationDao.clearActiveConfiguration()
        configurationDao.activateConfiguration(configuration.id, mode)
    }

    fun getActiveConfiguration(): Flow<Configuration?> = configurationDao.getActiveConfiguration()

    // funkcje, ktore operuja na powiazaniach
    suspend fun getResourcesWithImagesForActiveConfig(): List<ResourceWithImages> {
        val activeConfig =
            configurationDao.getActiveConfiguration().firstOrNull() ?: return emptyList()

        // Pobierz wszystkie powiązania ConfigurationResource
        val configResources = configurationResourceRepo.getManyByConfigurationId(activeConfig.id)
        val resourceIds = configResources.map { it.resourceId }

        // Pobierz wszystkie zasoby z obrazami i filtruj po resourceIds
        val allResourcesWithImages = resourceRepo.getResourcesWithImages()
        return allResourcesWithImages.filter { it.resource.id in resourceIds }
    }

    suspend fun getMaterialState(configId: Long): ConfigurationMaterialState {
        val resources = getResourcesWithImagesForActiveConfig()
        return ConfigurationMaterialState(
            vocabItems = resources.map { res ->
                VocabularyItem(
                    id = res.resource.id,
                    word = res.resource.name,
                    learnedWord = res.resource.learnedWord,
                    selectedImages = List(res.images.size) { true },
                    inLearningStates = List(res.images.size) { true },
                    inTestStates = List(res.images.size) { false },
                    imagePaths = res.images.map { it.path }
                )
            }
        )
    }

    suspend fun getReinforcementState(configId: Long): ConfigurationReinforcementState {
        // Tymczasowo np. stałe wartości:
        return ConfigurationReinforcementState(
            praiseStates = mapOf(
                "dobrze" to true,
                "super" to true,
                "świetnie" to true,
                "ekstra" to true,
                "rewelacja" to true,
                "brawo" to true
            ),
            animationsEnabled = true
        )
    }

    suspend fun hasMaterialsForActiveConfig(isTestMode: Boolean): Boolean {
        return getResourcesWithImagesForActiveConfigFiltered(isTestMode).isNotEmpty()
    }
    suspend fun getConfigurationNamesUsingResource(resourceId: Long): List<String> {
        val links = configurationResourceDao.getByResourceId(resourceId)
        if (links.isEmpty()) return emptyList()

        val configIds = links.map { it.configurationId }
        val configs = dao.getByIds(configIds)   // patrz niżej
        return configs.map { it.name }
    }

    suspend fun getResourcesWithImagesForActiveConfigFiltered(isTestMode: Boolean): List<ResourceWithImages> {
        val activeConfig = dao.getActiveConfiguration().firstOrNull() ?: return emptyList()

        // zasoby przypięte do tej konfiguracji
        val configResources = dao.getConfigurationResources(activeConfig.id)
        val resourceIds = configResources.map { it.resourceId }.toSet()

        // usage obrazów dla tej konfiguracji
        val usages = dao.getConfigurationImageUsages(activeConfig.id)
        val allowedImageIds = usages
            .filter { usage -> if (isTestMode) usage.inTest else usage.inLearning }
            .map { it.imageId }
            .toSet()

        // pełna lista zasobów z obrazami
        val all = resourceDao.getResourcesWithImages()

        // filtr: tylko zasoby z konfiguracji + odfiltrowane obrazy wg trybu
        return all
            .filter { it.resource.id in resourceIds }
            .map { rwi -> rwi.copy(images = rwi.images.filter { it.id in allowedImageIds }) }
            .filter { it.images.isNotEmpty() }
    }

}
