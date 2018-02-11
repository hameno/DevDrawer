package de.psdev.devdrawer.database

import androidx.recyclerview.widget.DiffUtil
import androidx.room.*
import de.psdev.devdrawer.appwidget.PackageHashInfo
import java.util.*

@Entity(
    tableName = "filters",
    foreignKeys = [
        ForeignKey(
            entity = WidgetProfile::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PackageFilter(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "type")
    val type: FilterType = FilterType.PACKAGE_NAME,
    @ColumnInfo(name = "filter")
    val filter: String,
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "profile_id", index = true)
    val profileId: String
) {
    companion object {
        val DIFF_CALLBACK = object: DiffUtil.ItemCallback<PackageFilter>() {
            override fun areItemsTheSame(oldItem: PackageFilter, newItem: PackageFilter): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: PackageFilter, newItem: PackageFilter): Boolean = oldItem.filter == newItem.filter
        }
    }

    @delegate:Ignore
    private val filterRegex: Regex by lazy { filter.replace("*", ".*").toRegex() }

    fun matches(packageHashInfo: PackageHashInfo): Boolean = when (type) {
        FilterType.PACKAGE_NAME -> filterRegex.matches(packageHashInfo.packageName)
        FilterType.SIGNATURE -> filter == packageHashInfo.signatureHashSha256
    }

}

