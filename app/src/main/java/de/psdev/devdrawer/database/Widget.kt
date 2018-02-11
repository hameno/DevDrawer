package de.psdev.devdrawer.database

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "widgets",
    foreignKeys = [
        ForeignKey(
            entity = WidgetProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"]
        )
    ]
)
data class Widget(
    @PrimaryKey
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    val id: Int,
    @ColumnInfo(name = "name", index = true)
    var name: String,
    @ColumnInfo(name = "color", typeAffinity = ColumnInfo.INTEGER)
    var color: Int,
    @ColumnInfo(name = "profile_id", index = true)
    var profileId: String
) {
    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<Widget>() {
            override fun areItemsTheSame(oldItem: Widget, newItem: Widget): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Widget, newItem: Widget): Boolean = oldItem == newItem
        }
    }
}