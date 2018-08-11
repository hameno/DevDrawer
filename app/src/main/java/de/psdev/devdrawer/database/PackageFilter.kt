package de.psdev.devdrawer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filters")
data class PackageFilter(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.INTEGER)
    val id: Int = 0,
    @ColumnInfo(name = "filter", typeAffinity = ColumnInfo.TEXT)
    val filter: String)