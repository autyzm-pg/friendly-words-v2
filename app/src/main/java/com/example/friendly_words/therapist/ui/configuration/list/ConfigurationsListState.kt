package com.example.friendly_words.therapist.ui.configuration.list

import com.example.shared.data.entities.Configuration

data class ConfigurationState(
    val searchQuery: String = "",
    val configurations: List<Configuration> = emptyList(),
    val activeConfiguration: Configuration? = null,
    val showDeleteDialogFor: Configuration? = null,
    val showActivateDialogFor: Configuration? = null,
    val showCopyDialogFor: Configuration? = null,
    val shouldScrollToBottom: Boolean=false,
    val infoMessage: String? = null,
    val newlyAddedConfigId: Long? = null,
    val shouldScrollToTop: Boolean = false,
    val hideExamples: Boolean = false
)