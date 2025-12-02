package com.example.friendly_words.therapist.ui.configuration.list

import com.example.shared.data.entities.Configuration

sealed class ConfigurationEvent {
    data class SearchChanged(val query: String) : ConfigurationEvent()
    data class DeleteRequested(val configuration: Configuration) : ConfigurationEvent()
    data class ConfirmDelete(val configuration: Configuration) : ConfigurationEvent()
    data class ActivateRequested(val configuration: Configuration) : ConfigurationEvent()
    data class ConfirmActivate(val configuration: Configuration) : ConfigurationEvent()
    data class EditRequested(val configuration: Configuration) : ConfigurationEvent()
    object CreateRequested : ConfigurationEvent()
    object ScrollToTopHandled : ConfigurationEvent()
    object ClearInfoMessage : ConfigurationEvent()
    data class SetNewlyAddedId(val id: Long) : ConfigurationEvent()
    object DismissDialogs : ConfigurationEvent()
    data class CopyRequested(val configuration: Configuration) : ConfigurationEvent()
    data class ConfirmCopy(val configuration: Configuration) : ConfigurationEvent()
    object ScrollHandled : ConfigurationEvent()
    data class SetActiveMode(val configuration: Configuration, val mode: String) : ConfigurationEvent()
    data class ToggleHideExamples(val hide: Boolean) : ConfigurationEvent()
}