package de.psdev.devdrawer.utils

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun <T, VH : RecyclerView.ViewHolder> ListAdapter<T, VH>.awaitSubmit(
    list: List<T>
) = suspendCancellableCoroutine<Unit> { cont ->
    submitList(list) {
        cont.resume(value = Unit)
    }
}