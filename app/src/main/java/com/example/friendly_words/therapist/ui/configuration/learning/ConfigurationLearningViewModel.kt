package com.example.friendly_words.therapist.ui.configuration.learning

import androidx.lifecycle.ViewModel
import com.example.shared.data.another.ConfigurationLearningState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConfigurationLearningViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ConfigurationLearningState())
    val state: StateFlow<ConfigurationLearningState> = _state

    fun onEvent(event: ConfigurationLearningEvent) {
        _state.update { reduce(it, event) }
    }

    companion object {
        fun reduce(
            state: ConfigurationLearningState,
            event: ConfigurationLearningEvent
        ): ConfigurationLearningState {
            val selectedCount = listOf(
                state.outlineCorrect,
                state.animateCorrect,
                state.scaleCorrect,
                state.dimIncorrect
            ).count { it }

            return when (event) {
                is ConfigurationLearningEvent.SetImageCount -> state.copy(imageCount = event.count)
                is ConfigurationLearningEvent.SetRepetitionCount -> state.copy(repetitionCount = event.count)
                is ConfigurationLearningEvent.SetTimeCount -> state.copy(timeCount = event.count)
                is ConfigurationLearningEvent.SetPrompt -> state.copy(selectedPrompt = event.prompt)
                is ConfigurationLearningEvent.ToggleCaptions -> state.copy(captionsEnabled = event.enabled)
                is ConfigurationLearningEvent.ToggleReading -> state.copy(readingEnabled = event.enabled)

                is ConfigurationLearningEvent.ToggleOutlineCorrect ->
                    if (event.enabled || selectedCount > 1) state.copy(outlineCorrect = event.enabled) else state

                is ConfigurationLearningEvent.ToggleAnimateCorrect ->
                    if (event.enabled || selectedCount > 1) state.copy(animateCorrect = event.enabled) else state

                is ConfigurationLearningEvent.ToggleScaleCorrect ->
                    if (event.enabled || selectedCount > 1) state.copy(scaleCorrect = event.enabled) else state

                is ConfigurationLearningEvent.ToggleDimIncorrect ->
                    if (event.enabled || selectedCount > 1) state.copy(dimIncorrect = event.enabled) else state
            }
        }
    }
}