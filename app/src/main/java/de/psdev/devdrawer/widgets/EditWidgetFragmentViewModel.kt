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
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import mu.KLogging
import kotlin.random.Random

class EditWidgetFragmentViewModel @ViewModelInject constructor(
    private val database: DevDrawerDatabase,
    @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel() {

    companion object: KLogging();

    // TODO Replace by safer method once https://github.com/google/dagger/issues/1906 is solved
    val widgetId = savedStateHandle.get<Int>("widgetId") ?: throw IllegalStateException("No widgetId")

    // Inputs
    val inputWidgetName = MutableStateFlow("")
    val inputSelectedProfile = MutableStateFlow<Selection>(Selection.Nothing)
    val inputSaveTrigger = BroadcastChannel<Unit>(1)

    // Outputs
    val outputWidgetProfiles
        get() = database.widgetProfileDao().findAllFlow()

    val outputFormCompleted = combine(inputWidgetName, inputSelectedProfile) { name, selection ->
        name.isNotBlank() && selection is Selection.Profile
    }

    // TODO add sealed class for success / cancel
    val outputCloseTrigger = BroadcastChannel<Widget>(1)

    val savedWidget: MutableStateFlow<Widget?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            savedWidget.value = database.widgetDao().findById(widgetId)
            inputSaveTrigger.asFlow().flatMapLatest {
                inputWidgetName.zip(inputSelectedProfile.filterIsInstance<Selection.Profile>()) { name, selection ->
                    savedWidget.value?.copy(
                        name = name,
                        profileId = selection.profile.id
                    ) ?: Widget(id = widgetId, name = name, color = colors[Random.nextInt(colors.size)], profileId = selection.profile.id)
                }
            }.onEach { widget ->
                database.widgetDao().insertOrUpdate(widget)
                savedWidget.value = widget
                outputCloseTrigger.offer(widget)
            }.launchIn(this)
        }
    }

    private val colors = listOf(
        Color.RED,
        Color.BLACK,
        Color.BLUE,
        Color.GREEN
    )

    sealed class Selection {
        object Nothing: Selection()
        data class Profile(val profile: WidgetProfile): Selection()
    }

}