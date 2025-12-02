package com.example.friendly_words.therapist.ui.configuration.settings

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendly_words.therapist.data.PreferencesRepository   // 👈 DODANE
import com.example.shared.data.entities.Configuration
import com.example.shared.data.entities.ConfigurationResource
import com.example.shared.data.entities.toConfigurationLearningState
import com.example.shared.data.entities.toConfigurationReinforcementState
import com.example.shared.data.entities.toConfigurationTestState
import com.example.shared.data.repositories.ConfigurationRepository
import com.example.shared.data.repositories.ImageRepository
import com.example.shared.data.repositories.ResourceRepository
import com.example.friendly_words.therapist.ui.configuration.learning.*
import com.example.friendly_words.therapist.ui.configuration.material.*
import com.example.friendly_words.therapist.ui.configuration.reinforcement.ConfigurationReinforcementViewModel
import com.example.friendly_words.therapist.ui.configuration.save.ConfigurationSaveEvent
import com.example.friendly_words.therapist.ui.configuration.save.ConfigurationSaveViewModel
import com.example.friendly_words.therapist.ui.configuration.test.ConfigurationTestEvent
import com.example.friendly_words.therapist.ui.configuration.test.ConfigurationTestViewModel
import com.example.shared.data.another.ConfigurationMaterialState
import com.example.shared.data.another.VocabularyItem
import com.example.shared.data.another.toConfigurationImageUsages
import com.example.shared.data.another.toDerivedTestState
import com.example.shared.data.another.toLearningSettings
import com.example.shared.data.another.toTestSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationSettingsViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
    private val resourceRepository: ResourceRepository,
    private val imageRepository: ImageRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ConfigurationSettingsState())
    val state: StateFlow<ConfigurationSettingsState> = _state

    init {
        viewModelScope.launch {
            updateAvailableWordsToAdd()
        }

        viewModelScope.launch {
            preferencesRepository.hideExampleMaterialsFlow.collect { hide ->
                _state.update {
                    it.copy(hideExamples = hide)
                }
                updateAvailableWordsToAdd()
            }
        }
    }

    fun onEvent(event: ConfigurationSettingsEvent) {
        when (event) {
            is ConfigurationSettingsEvent.Learning -> {
                _state.update { current ->
                    current.copy(
                        learningState = ConfigurationLearningViewModel.reduce(
                            current.learningState,
                            event.event
                        )
                    )
                }
            }

            is ConfigurationSettingsEvent.Material -> {
                when (val materialEvent = event.event) {
                    is ConfigurationMaterialEvent.AddWord -> {
                        viewModelScope.launch {
                            val resource = resourceRepository.getById(materialEvent.id)
                            val images = imageRepository.getByResourceId(resource.id)

                            val newVocabularyItem = VocabularyItem(
                                id = resource.id,
                                word = resource.name,
                                learnedWord = resource.learnedWord,
                                selectedImages = List(images.size) { true },
                                inLearningStates = List(images.size) { true },
                                inTestStates = List(images.size) { true },
                                imagePaths = images.map { it.path }
                            )

                            _state.update { current ->
                                val updatedItems = current.materialState.vocabItems + newVocabularyItem
                                current.copy(
                                    materialState = current.materialState.copy(
                                        vocabItems = updatedItems,
                                        selectedWordIndex = updatedItems.lastIndex,
                                        showAddDialog = false
                                    )
                                )
                            }

                            logUsedImages()
                            updateAvailableWordsToAdd()
                        }
                    }

                    is ConfigurationMaterialEvent.ConfirmDelete -> {
                        viewModelScope.launch {
                            val oldState = _state.value.materialState
                            val updatedList = oldState.vocabItems.toMutableList().apply {
                                removeAt(materialEvent.index)
                            }
                            val newIndex = when {
                                oldState.selectedWordIndex == materialEvent.index ->
                                    if (updatedList.isNotEmpty()) 0 else -1
                                oldState.selectedWordIndex > materialEvent.index ->
                                    oldState.selectedWordIndex - 1
                                else -> oldState.selectedWordIndex
                            }

                            _state.update {
                                it.copy(
                                    materialState = it.materialState.copy(
                                        vocabItems = updatedList,
                                        selectedWordIndex = newIndex,
                                        wordIndexToDelete = null,
                                        showDeleteDialog = false
                                    )
                                )
                            }
                            updateAvailableWordsToAdd()
                        }
                    }

                    else -> {
                        _state.update { current ->
                            current.copy(
                                materialState = ConfigurationMaterialViewModel.reduce(
                                    current.materialState,
                                    materialEvent
                                )
                            )
                        }
                    }
                }
            }

            is ConfigurationSettingsEvent.Reinforcement -> {
                _state.update { current ->
                    current.copy(
                        reinforcementState = ConfigurationReinforcementViewModel.reduce(
                            current.reinforcementState,
                            event.event
                        )
                    )
                }
            }

            is ConfigurationSettingsEvent.ResetNavigation -> {
                _state.update {
                    it.copy(navigateToList = false, message = null)
                }
            }

            is ConfigurationSettingsEvent.Test -> {
                _state.update { current ->
                    val learning = current.learningState
                    val updatedTest = ConfigurationTestViewModel.reduce(
                        current.testState,
                        event.event,
                        if (
                            event.event is ConfigurationTestEvent.SetEditEnabled ||
                            event.event is ConfigurationTestEvent.ToggleTestEdit
                        ) learning else null
                    )
                    current.copy(testState = updatedTest)
                }
            }

            is ConfigurationSettingsEvent.Save -> {
                when (val saveEvent = event.event) {
                    is ConfigurationSaveEvent.SaveConfiguration -> {
                        viewModelScope.launch {
                            val currentState = state.value

                            val learningSettings = currentState.learningState.toLearningSettings(
                                materialState = currentState.materialState,
                                reinforcementState = currentState.reinforcementState
                            )

                            val testSettings = currentState.testState.toTestSettings(
                                materialState = currentState.materialState
                            )

                            val existingConfigurations = configurationRepository.getAllOnce()

                            val currentName = saveEvent.name.trim()
                            val currentId = currentState.saveState.editingConfigId
                            val alreadyExists = existingConfigurations.any {
                                it.name.equals(saveEvent.name, ignoreCase = true) &&
                                        (currentId == null || it.id != currentId)
                            }

                            if (alreadyExists) {
                                _state.update {
                                    it.copy(
                                        saveState = it.saveState.copy(showDuplicateNameDialog = true)
                                    )
                                }
                                return@launch
                            }

                            var insertedId: Long? = null

                            if (currentId != null) {
                                // edycja istniejącej
                                val originalConfiguration = existingConfigurations.first { it.id == currentId }

                                val updated = originalConfiguration.copy(
                                    name = currentName,
                                    isExample = false,
                                    learningSettings = learningSettings,
                                    testSettings = testSettings
                                )

                                configurationRepository.update(updated)

                                // nadpisujemy powiązania
                                configurationRepository.deleteResourcesByConfigId(currentId)
                                configurationRepository.deleteImageUsagesByConfigId(currentId)

                                val resourceLinks = currentState.materialState.vocabItems
                                    .map { it.id }
                                    .distinct()
                                    .map {
                                        ConfigurationResource(
                                            configurationId = currentId,
                                            resourceId = it
                                        )
                                    }
                                configurationRepository.insertResources(resourceLinks)

                                val imageUsages =
                                    currentState.materialState.toConfigurationImageUsages(
                                        configurationId = currentId,
                                        imageRepository = imageRepository
                                    )
                                configurationRepository.insertImageUsages(imageUsages)

                            } else {
                                // nowa konfiguracja
                                val configuration = Configuration(
                                    name = saveEvent.name,
                                    isExample = false,
                                    learningSettings = learningSettings,
                                    testSettings = testSettings
                                )

                                val newId = configurationRepository.insert(configuration)
                                insertedId = newId

                                val resourceLinks = currentState.materialState.vocabItems
                                    .map { it.id }
                                    .distinct()
                                    .map {
                                        ConfigurationResource(
                                            configurationId = newId,
                                            resourceId = it
                                        )
                                    }
                                configurationRepository.insertResources(resourceLinks)

                                val imageUsages =
                                    currentState.materialState.toConfigurationImageUsages(
                                        configurationId = newId,
                                        imageRepository = imageRepository
                                    )
                                configurationRepository.insertImageUsages(imageUsages)
                            }

                            val message = if (currentId != null) {
                                "Pomyślnie zaktualizowano krok uczenia"
                            } else {
                                "Pomyślnie dodano nowy krok uczenia"
                            }
                            _state.update {
                                it.copy(
                                    lastSavedConfigId = insertedId ?: it.lastSavedConfigId,
                                    message = message,
                                    navigateToList = true
                                )
                            }
                        }
                    }

                    else -> {
                        _state.update {
                            it.copy(
                                saveState = ConfigurationSaveViewModel.reduce(
                                    it.saveState,
                                    saveEvent
                                )
                            )
                        }
                    }
                }
            }

            is ConfigurationSettingsEvent.ShowExitDialog -> {
                _state.update { it.copy(showExitDialog = true) }
            }

            is ConfigurationSettingsEvent.CancelExitDialog -> {
                _state.update { it.copy(showExitDialog = false) }
            }

            is ConfigurationSettingsEvent.ConfirmExitDialog -> {
                _state.value = ConfigurationSettingsState()
            }
        }
    }

    // odświeża listę dostępnych słów w dialogu DODAJ
    private suspend fun updateAvailableWordsToAdd() {
        val allResources = resourceRepository.getAllOnce()
        val current = _state.value
        val usedIds = current.materialState.vocabItems.map { it.id }

        var available = allResources.filter { it.id !in usedIds }

        if (current.hideExamples) {
            available = available.filter { !it.isExample }
        }

        _state.update {
            it.copy(
                materialState = it.materialState.copy(availableWordsToAdd = available)
            )
        }
    }

    private fun logUsedImages() {
        val usedImages = getDetailedImageUsage().filter { it.isSelected }

        val logMessage = buildString {
            append("Użyte zdjęcia:\n")
            usedImages.forEach {
                val mode = when {
                    it.inLearning && it.inTest -> "Learning + Test"
                    it.inLearning -> "Learning"
                    it.inTest -> "Test"
                    else -> "brak"
                }
                append("- Słowo: ${it.word} | Zdjęcie: ${it.imagePath} | Tryb: $mode\n")
            }
        }

        Log.d("ImageUsageInfo", logMessage)
    }

    fun getDetailedImageUsage(): List<ImageUsageInfo> {
        return _state.value.materialState.vocabItems.flatMap { item ->
            item.imagePaths.mapIndexed { index, path ->
                ImageUsageInfo(
                    word = item.word,
                    imagePath = path,
                    isSelected = item.selectedImages.getOrNull(index) == true,
                    inLearning = item.inLearningStates.getOrNull(index) == true,
                    inTest = item.inTestStates.getOrNull(index) == true
                )
            }
        }
    }

    fun loadConfiguration(configId: Long) {
        viewModelScope.launch {
            val config = configurationRepository.getById(configId)
            val linkResources = configurationRepository.getResources(configId)
            val linkImages = configurationRepository.getImageUsages(configId)

            val vocabItems = linkResources.map { link ->
                val resource = resourceRepository.getById(link.resourceId)
                val dbImages = imageRepository.getByResourceId(link.resourceId)

                val selected = MutableList(dbImages.size) { false }
                val inLearningLst = MutableList(dbImages.size) { false }
                val inTestLst = MutableList(dbImages.size) { false }

                dbImages.forEachIndexed { idx, img ->
                    linkImages.find { it.imageId == img.id }?.let { usage ->
                        selected[idx] = true
                        inLearningLst[idx] = usage.inLearning
                        inTestLst[idx] = usage.inTest
                    }
                }

                VocabularyItem(
                    id = resource.id,
                    word = resource.name,
                    learnedWord = resource.learnedWord,
                    selectedImages = selected,
                    inLearningStates = inLearningLst,
                    inTestStates = inTestLst,
                    imagePaths = dbImages.map { it.path }
                )
            }

            val materialState = ConfigurationMaterialState(vocabItems = vocabItems)
            val learningState = config.learningSettings.toConfigurationLearningState()
            val testState = config.testSettings.toConfigurationTestState()
            val testShouldBeEditable = testState != learningState.toDerivedTestState()
            val finalTestState = testState.copy(testEditEnabled = testShouldBeEditable)
            val reinforcementState = config.learningSettings.toConfigurationReinforcementState()

            _state.update {
                it.copy(
                    materialState = materialState,
                    learningState = learningState,
                    testState = finalTestState,
                    reinforcementState = reinforcementState,
                    saveState = it.saveState.copy(
                        stepName = TextFieldValue(
                            text = config.name,
                            selection = TextRange(config.name.length)
                        ),
                        editingConfigId = config.id
                    )
                )
            }
            updateAvailableWordsToAdd()
        }
    }
}
