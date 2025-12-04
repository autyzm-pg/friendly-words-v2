package com.example.friendly_words.therapist.ui.configuration.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendly_words.therapist.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.shared.data.another.ConfigurationMaterialState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImageUsageInfo(
    val word: String,
    val imagePath: String,
    val isSelected: Boolean,
    val inLearning: Boolean,
    val inTest: Boolean
)
@HiltViewModel
class ConfigurationMaterialViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConfigurationMaterialState())

    private val _hideExamples = MutableStateFlow(false)
    val hideExamples: StateFlow<Boolean> = _hideExamples

    init {
        viewModelScope.launch {
            preferencesRepository.hideExampleMaterialsFlow.collect { hide ->
                _hideExamples.value = hide
            }
        }
    }

    fun onEvent(event: ConfigurationMaterialEvent) {
        _uiState.update { current ->
            reduce(current, event)
        }
    }

    companion object {
        fun reduce(
            state: ConfigurationMaterialState,
            event: ConfigurationMaterialEvent
        ): ConfigurationMaterialState {
            return when (event) {

                is ConfigurationMaterialEvent.WordDeleted -> state.copy(wordIndexToDelete = event.index, showDeleteDialog = true)

                is ConfigurationMaterialEvent.WordSelected -> state.copy(selectedWordIndex = event.index)

                is ConfigurationMaterialEvent.CancelDelete -> state.copy(
                    wordIndexToDelete = null,
                    showDeleteDialog = false
                )

                is ConfigurationMaterialEvent.ShowAddDialog -> state.copy(showAddDialog = true)

                is ConfigurationMaterialEvent.HideAddDialog -> state.copy(showAddDialog = false)

                is ConfigurationMaterialEvent.ImageSelectionChanged -> {
                    val updatedItems = state.vocabItems.mapIndexed { index, item ->
                        if (index == state.selectedWordIndex)
                            item.copy(selectedImages = event.selected)
                        else item
                    }
                    state.copy(vocabItems = updatedItems)
                }

                is ConfigurationMaterialEvent.LearningTestChanged -> {
                    val updatedItems = state.vocabItems.mapIndexed { index, item ->
                        if (index == state.selectedWordIndex) {
                            item.copy(
                                inLearningStates = item.inLearningStates.toMutableList().also {
                                    it[event.index] = event.inLearning
                                },
                                inTestStates = item.inTestStates.toMutableList().also {
                                    it[event.index] = event.inTest
                                }
                            )
                        } else item
                    }
                    state.copy(vocabItems = updatedItems)
                }
                else -> state
            }
        }
    }
}
