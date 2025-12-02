package com.example.shared.data.another

import com.example.shared.data.entities.TestSettings

data class ConfigurationTestState(
    val imageCount: Int = 3,
    val repetitionCount: Int = 0,
    val answerTime: Int = 0,
    val testEditEnabled: Boolean = false,
    val selectedPrompt: String = "{Słowo}",
    val captionsEnabled: Boolean = true,
    val readingEnabled: Boolean = true
)

fun ConfigurationTestState.toTestSettings(
    materialState: ConfigurationMaterialState
): TestSettings {
    return TestSettings(
        numberOfWords = materialState.vocabItems.size,
        displayedImagesCount = this.imageCount,
        repetitionPerWord = this.repetitionCount,
        commandType = when (this.selectedPrompt) {
            "{Słowo}" -> "SHORT"
            "Gdzie jest {Słowo}" -> "WHERE_IS"
            "Pokaż gdzie jest {Słowo}" -> "SHOW_ME"
            else -> "SHORT"
        },
        showLabelsUnderImages = this.captionsEnabled,
        readCommand = this.readingEnabled,
        answerTimeSeconds      = this.answerTime
    )
}
fun ConfigurationLearningState.toDerivedTestState(): ConfigurationTestState {
    return ConfigurationTestState(
        imageCount = this.imageCount,
        repetitionCount = this.repetitionCount,
        selectedPrompt = this.selectedPrompt,
        captionsEnabled = this.captionsEnabled,
        readingEnabled = this.readingEnabled,
        testEditEnabled = false,
        answerTime = this.timeCount
    )
}

