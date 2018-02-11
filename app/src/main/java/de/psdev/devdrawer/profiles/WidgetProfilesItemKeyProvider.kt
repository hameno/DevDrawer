package de.psdev.devdrawer.profiles

import androidx.recyclerview.selection.ItemKeyProvider

class WidgetProfilesItemKeyProvider(private val adapter: WidgetProfilesListAdapter): ItemKeyProvider<String>(
    SCOPE_MAPPED
) {
    override fun getKey(position: Int): String? = adapter.getItem(position).id
    override fun getPosition(key: String): Int = adapter.currentList.indexOfFirst { it.id == key }
}