package de.psdev.devdrawer.widgets

import android.graphics.Color
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.psdev.devdrawer.database.DevDrawerDatabase
import de.psdev.devdrawer.database.Widget
import de.psdev.devdrawer.database.WidgetProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KLogging

class EditWidgetFragmentViewModel @ViewModelInject constructor(
        private val database: DevDrawerDatabase,
        @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object : KLogging()

    // TODO Replace by safer method once https://github.com/google/dagger/issues/1906 is solved
    val widgetId = savedStateHandle.get<Int>("widgetId")
            ?: throw IllegalStateException("No widgetId")

    // Inputs
    val inputWidgetName = MutableStateFlow("")
    val inputColor = MutableStateFlow(Color.BLACK)
    val inputSelectedProfile = MutableStateFlow<Selection>(Selection.Nothing)
    val inputSaveTrigger = MutableSharedFlow<Unit>(1)

    // Outputs
    val outputWidgetProfiles
        get() = database.widgetProfileDao().findAllFlow()

    val outputFormCompleted = combine(inputWidgetName, inputSelectedProfile) { name, selection ->
        name.isNotBlank() && selection is Selection.Profile
    }

    // TODO add sealed class for success / cancel
    val outputCloseTrigger = MutableSharedFlow<Widget>(1)

    val savedWidget: MutableStateFlow<Widget?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            savedWidget.value = database.widgetDao().findById(widgetId)?.also { widget ->
                inputWidgetName.value = widget.name
                inputColor.value = widget.color
            }
        }
        combine(inputWidgetName, inputColor) { name, color ->
            logger.info { "Update savedWidget: $name, $color" }
            savedWidget.value?.let { widget ->
                widget.name = name
                widget.color = color
            }
        }.launchIn(viewModelScope)
        inputSaveTrigger.asSharedFlow().flatMapLatest {
            combine(
                    inputWidgetName,
                    inputColor,
                    inputSelectedProfile.filterIsInstance<Selection.Profile>()
            ) { name, color, selection ->
                savedWidget.value?.copy(
                        name = name,
                        color = color,
                        profileId = selection.profile.id
                ) ?: Widget(
                        id = widgetId,
                        name = name,
                        color = color,
                        profileId = selection.profile.id
                )
            }
        }.onEach { widget ->
            database.widgetDao().insertOrUpdate(widget)
            savedWidget.value = widget
            outputCloseTrigger.emit(widget)
        }.launchIn(viewModelScope)
    }

    sealed class Selection {
        object Nothing : Selection()
        data class Profile(val profile: WidgetProfile) : Selection()
    }

}