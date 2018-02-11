package de.psdev.devdrawer.utils

import android.widget.Button
import android.widget.EditText
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.android.widget.textChanges

fun MutableStateFlow<String>.receiveTextChangesFrom(editText: EditText) = editText.textChanges()
    .map { it.toString() }
    .onEach { value = it }

fun SendChannel<Unit>.receiveClicksFrom(button: Button) = button.clicks()
    .onEach { offer(it) }
