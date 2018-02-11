package de.psdev.devdrawer.database

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "widget_profiles")
data class WidgetProfile(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "name", typeAffinity = ColumnInfo.TEXT)
    var name: String
) {
    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<WidgetProfile>() {
            override fun areItemsTheSame(oldItem: WidgetProfile, newItem: WidgetProfile): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: WidgetProfile, newItem: WidgetProfile): Boolean = oldItem == newItem
        }
    }
}
