package de.psdev.devdrawer.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PackageFilter::class], version = DevDrawerDatabase.VERSION)
abstract class DevDrawerDatabase: RoomDatabase() {

    companion object {
        const val NAME = "DevDrawer.db"
        const val VERSION = 1
    }

    abstract fun packageFilterDao(): PackageFilterDao

}