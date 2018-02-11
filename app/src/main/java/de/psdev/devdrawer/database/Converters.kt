package de.psdev.devdrawer.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFilterType(filterType: FilterType?): String? = filterType?.name

    @TypeConverter
    fun toFilterType(value: String?): FilterType? = value?.let { FilterType.valueOf(it) }
}
