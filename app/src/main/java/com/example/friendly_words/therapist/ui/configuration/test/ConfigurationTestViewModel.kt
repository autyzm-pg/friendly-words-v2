package com.example.friendly_words.therapist.ui.configuration.test

import androidx.lifecycle.ViewModel
import com.example.shared.data.another.ConfigurationLearningState
import com.example.shared.data.another.ConfigurationTestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConfigurationTestViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ConfigurationTestState())
    val state: StateFlow<ConfigurationTestState> = _state

    fun onEvent(event: ConfigurationTestEvent, learningState: ConfigurationLearningState? = null) {
        _state.update {
            reduce(it, event, learningState)
        }
    }

    companion object {
        fun reduce(state: ConfigurationTestState, event: ConfigurationTestEvent, learningState: ConfigurationLearningState? = null): ConfigurationTestState {
            return when (event) {
                is ConfigurationTestEvent.SetRepetitionCount -> state.copy(repetitionCount = event.count)
                is ConfigurationTestEvent.SetImageCount -> state.copy(imageCount = event.count)
                is ConfigurationTestEvent.SetEditEnabled -> {
                    if (!event.enabled && learningState != null) {
                        state.copy(
                            testEditEnabled = false,
                            imageCount = learningState.imageCount,
                            repetitionCount = learningState.repetitionCount,
                            selectedPrompt = learningState.selectedPrompt,
                            captionsEnabled = learningState.captionsEnabled,
                            readingEnabled = learningState.readingEnabled
                        )
                    } else {
                        state.copy(testEditEnabled = event.enabled)
                    }
                }
                is ConfigurationTestEvent.ToggleTestEdit -> {
                    val newEditValue = !state.testEditEnabled
                    if (!newEditValue && learningState != null) {
                        state.copy(
                            testEditEnabled = false,
                            imageCount = learningState.imageCount,
                            repetitionCount = learningState.repetitionCount,
                            selectedPrompt = learningState.selectedPrompt,
                            captionsEnabled = learningState.captionsEnabled,
                            readingEnabled = learningState.readingEnabled
                        )
                    } else {
                        state.copy(testEditEnabled = newEditValue)
                    }
                }
                is ConfigurationTestEvent.SetPrompt -> state.copy(selectedPrompt = event.prompt)
                is ConfigurationTestEvent.ToggleCaptions -> state.copy(captionsEnabled = event.enabled)
                is ConfigurationTestEvent.ToggleReading -> state.copy(readingEnabled = event.enabled)
            }
        }
    }
}