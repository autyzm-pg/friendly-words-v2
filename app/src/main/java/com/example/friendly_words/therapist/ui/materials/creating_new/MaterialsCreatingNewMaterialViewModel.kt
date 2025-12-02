package com.example.friendly_words.therapist.ui.materials.creating_new

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.entities.Image
import com.example.shared.data.entities.Resource
import com.example.shared.data.repositories.ImageRepository
import com.example.shared.data.repositories.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@HiltViewModel
class MaterialsCreatingNewMaterialViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val imageRepository: ImageRepository,
    private val resourceRepository: ResourceRepository
)  : ViewModel() {

    private val resourceIdToEdit = savedStateHandle.get<Long?>("resourceId")
    private val imagesToUnlink = mutableListOf<Image>()

    private val _state = MutableStateFlow(
        MaterialsCreatingNewMaterialState(
            resourceName = "",
            learnedWord = TextFieldValue(""),
            images = emptyList(),
            isEditing = resourceIdToEdit != null
        )
    )
    val state: StateFlow<MaterialsCreatingNewMaterialState> = _state

    private fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Cannot open input stream for URI: $uri")

        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        return file.absolutePath
    }


    init {
        viewModelScope.launch {
            if (resourceIdToEdit != null) {
                val resource = resourceRepository.getById(resourceIdToEdit)
                val images = imageRepository.getByResourceId(resourceIdToEdit)
                _state.update {
                    it.copy(
                        resourceName = resource.name,
                        learnedWord = TextFieldValue(resource.learnedWord, selection = TextRange(resource.learnedWord.length)),
                        images = images,
                        category = resource.category
                    )
                }
            }
        }
    }

    fun onEvent(event: MaterialsCreatingNewMaterialEvent) {
        when (event) {
            is MaterialsCreatingNewMaterialEvent.ResourceNameChanged -> {
                _state.update { it.copy(resourceName = event.newName) }
            }
            is MaterialsCreatingNewMaterialEvent.AddImage -> {
                viewModelScope.launch {
                    val localPath = copyImageToInternalStorage(event.uri)

                    val image = Image(path = localPath)

                    _state.update {
                        it.copy(images = it.images + image)
                    }
                }
            }
            is MaterialsCreatingNewMaterialEvent.RemoveImage -> {
                if (resourceIdToEdit != null && event.image.id != 0L) {
                    imagesToUnlink.add(event.image) // dodaj do usunięcia przy zapisie
                }
                _state.update {
                    it.copy(images = it.images.filterNot { img -> img.path == event.image.path })
                }
            }
            is MaterialsCreatingNewMaterialEvent.ShowExitDialog -> {
                _state.update { it.copy(showExitConfirmation = true) }
            }
            is MaterialsCreatingNewMaterialEvent.DismissExitDialog -> {
                _state.update { it.copy(showExitConfirmation = false) }
            }
            is MaterialsCreatingNewMaterialEvent.ConfirmExit -> {
                viewModelScope.launch {
                    imageRepository.deleteUnassignedImages()
                    _state.update { it.copy(showExitConfirmation = false, exitWithoutSaving = true) }
                }
            }
            is MaterialsCreatingNewMaterialEvent.SaveClicked -> {
                viewModelScope.launch {
                    val name = state.value.resourceName.trim()
                    val learnedWord = state.value.learnedWord.text.trim()
                    val category = state.value.category.trim()

                    if (name.isBlank() && learnedWord.isBlank()) {
                        _state.update { it.copy(showEmptyTextFieldsDialog = true) }
                        return@launch
                    }


                    val allResources = resourceRepository.getAllOnce()
                    val alreadyExists = allResources.any {
                        it.name.equals(name, ignoreCase = true) && it.id != resourceIdToEdit
                    }

                    if (alreadyExists && !state.value.confirmingDuplicateSave) {

                        _state.update { it.copy(showDuplicateNameConfirmation = true) }
                        return@launch
                    }

                    val resourceId = if (resourceIdToEdit == null) {
                        resourceRepository.insert(Resource(name = name, learnedWord = learnedWord, category = category))
                    } else {
                        resourceRepository.update(Resource(id = resourceIdToEdit, name = name, learnedWord = learnedWord, category = category))
                        resourceIdToEdit
                    }

                    val imageIds = state.value.images.map { image ->
                        if (image.id == 0L) imageRepository.insert(image) else image.id
                    }

                    // tworzenie nowych powiązań (linkImagesToResource usuwa stare połączenia automatycznie)
                    imageRepository.linkImagesToResource(resourceId, imageIds)

                    // jeśli coś zostało oznaczone do odłączenia – usuwamy tylko powiązanie
                    if (imagesToUnlink.isNotEmpty()) {
                        val unlinkIds = imagesToUnlink.map { it.id }
                        imageRepository.unlinkImagesFromResource(resourceId!!, unlinkIds)
                        imagesToUnlink.clear()
                    }

                    imageRepository.deleteUnassignedImages()

                    _state.update {
                        it.copy(
                            saveCompleted = true,
                            newlySavedResourceId = resourceId,
                            showDuplicateNameConfirmation = false
                        )
                    }

                    savedStateHandle["newlySavedResourceId"] = resourceId
                    val successMessage = if (resourceIdToEdit == null)
                        "Pomyślnie dodano nowy materiał"
                    else
                        "Pomyślnie zaktualizowano materiał"

                    savedStateHandle["message"] = successMessage
                }
            }

            is MaterialsCreatingNewMaterialEvent.DismissDuplicateNameDialog -> {
                _state.update { it.copy(showDuplicateNameConfirmation = false) }
            }
            is MaterialsCreatingNewMaterialEvent.ConfirmSaveDespiteDuplicate -> {
                _state.update { it.copy(showDuplicateNameConfirmation = false, confirmingDuplicateSave = true) }
                onEvent(MaterialsCreatingNewMaterialEvent.SaveClicked)
            }
            is MaterialsCreatingNewMaterialEvent.ResetSaveCompleted -> {
                _state.update { it.copy(saveCompleted = false, newlySavedResourceId = null) }
            }
            is MaterialsCreatingNewMaterialEvent.ResetExitWithoutSaving -> {
                _state.update {
                    it.copy(
                        exitWithoutSaving = false,
                        images = emptyList()
                    )
                }
            }
            is MaterialsCreatingNewMaterialEvent.LearnedWordChanged -> {
                _state.update {
                    val newWord = event.word
                    val newName = if (!it.allowEditingResourceName) newWord.text else it.resourceName
                    it.copy(learnedWord = newWord, resourceName = newName)
                }
            }
            is MaterialsCreatingNewMaterialEvent.ToggleAllowEditingResourceName -> {
                _state.update {
                    val allowed = event.allowed
                    val newName = if (!allowed) it.learnedWord.text else it.resourceName
                    it.copy(allowEditingResourceName = allowed, resourceName = newName)
                }
            }
            is MaterialsCreatingNewMaterialEvent.ImageTakenFromCamera -> {
                _state.update {
                    it.copy(images = it.images + event.image)
                }
            }
            is MaterialsCreatingNewMaterialEvent.RequestImageDeletion -> {
                _state.update { it.copy(imageToConfirmDelete = event.image) }
            }
            is MaterialsCreatingNewMaterialEvent.CancelImageDeletion -> {
                _state.update { it.copy(imageToConfirmDelete = null) }
            }
            is MaterialsCreatingNewMaterialEvent.ConfirmImageDeletion -> {
                val image = state.value.imageToConfirmDelete
                if (image != null) {
                    if (resourceIdToEdit != null && image.id != 0L) {
                        imagesToUnlink.add(image)
                    }
                    _state.update {
                        it.copy(
                            images = it.images.filterNot { img -> img.path == image.path },
                            imageToConfirmDelete = null
                        )
                    }
                }
            }
            is MaterialsCreatingNewMaterialEvent.DismissEmptyFieldsDialog -> {
                _state.update { it.copy(showEmptyTextFieldsDialog = false) }
            }
            is MaterialsCreatingNewMaterialEvent.CategoryChanged -> {
                _state.update { it.copy(category = event.newCategory) }
            }
        }
    }
}