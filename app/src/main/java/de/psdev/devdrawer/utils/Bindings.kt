package de.psdev.devdrawer.utils

import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.android.widget.textChanges

fun MutableStateFlow<String>.receiveTextChangesFrom(editText: EditText) = editText.textChanges()
    .map { it.toString() }
    .onEach { value = it }

fun MutableSharedFlow<Unit>.receiveClicksFrom(button: Button) = button.clicks()
        .onEach { emit(it) }
