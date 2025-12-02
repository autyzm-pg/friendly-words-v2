package com.example.friendly_words.therapist.ui.configuration.learning

sealed class ConfigurationLearningEvent {
    data class SetImageCount(val count: Int) : ConfigurationLearningEvent()
    data class SetRepetitionCount(val count: Int) : ConfigurationLearningEvent()
    data class SetTimeCount(val count: Int) : ConfigurationLearningEvent()
    data class SetPrompt(val prompt: String) : ConfigurationLearningEvent()
    data class ToggleCaptions(val enabled: Boolean) : ConfigurationLearningEvent()
    data class ToggleReading(val enabled: Boolean) : ConfigurationLearningEvent()
    data class ToggleOutlineCorrect(val enabled: Boolean) : ConfigurationLearningEvent()
    data class ToggleAnimateCorrect(val enabled: Boolean) : ConfigurationLearningEvent()
    data class ToggleScaleCorrect(val enabled: Boolean) : ConfigurationLearningEvent()
    data class ToggleDimIncorrect(val enabled: Boolean) : ConfigurationLearningEvent()
}