package de.psdev.devdrawer.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory

fun <T : ViewModel> simpleFactory(block: () -> T): Factory = object : Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        modelClass: Class<T>
    ): T = block() as T
}