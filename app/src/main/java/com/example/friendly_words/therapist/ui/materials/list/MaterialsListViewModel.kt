package com.example.friendly_words.therapist.ui.materials.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendly_words.therapist.data.PreferencesRepository
import kotlinx.coroutines.flow.filterNotNull
import com.example.shared.data.repositories.ConfigurationRepository
import com.example.shared.data.repositories.ImageRepository
import com.example.shared.data.repositories.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaterialsListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resourceRepository: ResourceRepository,
    private val imageRepository: ImageRepository,
    private val configurationRepository: ConfigurationRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterialsListState())
    val uiState: StateFlow<MaterialsListState> = _uiState

    init {

        viewModelScope.launch {
            preferencesRepository.hideExampleMaterialsFlow.collect { hide ->
                _uiState.update {
                    it.copy(hideExamples = hide)
                }
            }
        }

        viewModelScope.launch {
            savedStateHandle
                .getStateFlow<Long?>("newlySavedResourceId", null)
                .filterNotNull()
                .collect { newId ->
                    _uiState.update { it.copy(pendingSelectId = newId) }
                    savedStateHandle["newlySavedResourceId"] = null
                }
        }
        viewModelScope.launch {
            resourceRepository.getAll().collect { rawResources ->

                val resources = rawResources.sortedBy { it.name.lowercase() }
                val imagesById = buildMap {
                    for (resource in resources) {
                        put(resource.id, imageRepository.getByResourceId(resource.id))
                    }
                }

                val pendingId = _uiState.value.pendingSelectId
                val indexToSelect = pendingId?.let { id ->
                    resources.indexOfFirst { it.id == id }.takeIf { it != -1 }
                }

                _uiState.update {
                    it.copy(
                        materials = resources,
                        imagesForSelected = imagesById,
                        selectedIndex = indexToSelect ?: it.selectedIndex?.coerceAtMost(resources.lastIndex),
                        pendingSelectId = null
                    )
                }
            }
        }
    }

    fun onEvent(event: MaterialsListEvent) {
        when (event) {
            is MaterialsListEvent.ClearInfoMessage -> {
                _uiState.update { it.copy(infoMessage = null) }
            }
            is MaterialsListEvent.SelectMaterial -> {
                val selected = event.index
                val selectedResource = _uiState.value.materials.getOrNull(selected) ?: return

                viewModelScope.launch {
                    val relatedImages = imageRepository.getByResourceId(selectedResource.id)

                    val updatedMap = _uiState.value.imagesForSelected.toMutableMap().apply {
                        this[selectedResource.id] = relatedImages
                    }

                    _uiState.update  {it.copy(
                        selectedIndex = selected,
                        imagesForSelected = updatedMap
                    )}
                }
            }
            is MaterialsListEvent.ToggleHideExamples -> {
                viewModelScope.launch {
                    preferencesRepository.setHideExampleMaterials(event.hide)
                }
            }
            is MaterialsListEvent.ShowUsedInConfigurations -> {
                _uiState.update {
                    it.copy(
                        showUsedInDialogFor = event.resource,
                        usedInConfigurations = event.configurations
                    )
                }
            }

            is MaterialsListEvent.RequestDelete -> {
                viewModelScope.launch {
                    val resource = event.resource

                    val configurations = configurationRepository
                        .getConfigurationNamesUsingResource(resource.id)

                    if (configurations.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                showUsedInDialogFor = resource,
                                usedInConfigurations = configurations,
                                materialToDelete = event.index to event.resource
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                showDeleteDialog = true,
                                materialToDelete = event.index to event.resource
                            )
                        }
                    }
                }
            }


            is MaterialsListEvent.SelectByResourceId -> {
                val idx = _uiState.value.materials.indexOfFirst { it.id == event.resourceId }
                if (idx != -1) {
                    onEvent(MaterialsListEvent.SelectMaterial(idx))
                }
            }
            is MaterialsListEvent.ConfirmDelete -> {
                val (index, resource) = _uiState.value.materialToDelete ?: return
                val currentSelected = _uiState.value.selectedIndex
                viewModelScope.launch {
                    resourceRepository.delete(resource)

                    val updatedMaterials = _uiState.value.materials.filter { it.id != resource.id }

                    val newSelectedIndex = when {
                        currentSelected == index -> updatedMaterials.indices.firstOrNull()
                        currentSelected != null && currentSelected > index -> currentSelected - 1
                        else -> currentSelected
                    }

                    _uiState.update {
                        it.copy(
                        selectedIndex = newSelectedIndex,
                        showDeleteDialog = false,
                        materialToDelete = null,
                        showUsedInDialogFor = null,
                        usedInConfigurations = null,
                        infoMessage = "Pomyślnie usunięto materiał"
                    )}
                }

            }
            is MaterialsListEvent.DismissDeleteDialog -> {
                _uiState.update {it.copy(
                    showDeleteDialog = false,
                    materialToDelete = null,
                    showUsedInDialogFor = null,
                    usedInConfigurations = null
                )}
            }
            is MaterialsListEvent.CopyRequested -> {
                _uiState.update { it.copy(showCopyDialogFor = event.resource) }
            }
            is MaterialsListEvent.ConfirmCopy -> {
                viewModelScope.launch {
                    val original = event.resource
                    val allNames = _uiState.value.materials.map { it.name }
                    val newName = generateCopyName(original.name, allNames)

                    val newId = resourceRepository.insert(
                        original.copy(
                            id = 0,
                            name = newName,
                            isExample = false
                        )
                    )

                    val images = imageRepository.getByResourceId(original.id)
                    val imageIds = images.map { it.id }
                    imageRepository.linkImagesToResource(newId, imageIds)

                    _uiState.update {
                        it.copy(
                            showCopyDialogFor = null,
                            infoMessage = "Skopiowano materiał: ${original.name}",
                            pendingSelectId = newId
                        )
                    }
                }
            }
            is MaterialsListEvent.DismissCopyDialog -> {
                _uiState.update {
                    it.copy(showCopyDialogFor = null)
                }
            }
        }
    }

    private fun generateCopyName(original: String, existing: List<String>): String {
        if (original !in existing) return original

        var index = 1
        var newName: String
        do {
            newName = "${original}_$index"
            index++
        } while (newName in existing)
        return newName
    }
}