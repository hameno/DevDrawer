package de.psdev.devdrawer.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

abstract class BaseDao<T> {

    @Insert
    abstract suspend fun insert(obj: T)

    @Insert
    abstract suspend fun insert(vararg obj: T)

    @Update
    abstract suspend fun update(obj: T)

    @Delete
    abstract suspend fun delete(obj: T)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnore(obj: T): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun updateOrAbort(obj: T)

    @Transaction
    open suspend fun insertOrUpdate(obj: T) {
        val id = insertOrIgnore(obj)
        if (id == -1L) {
            // Not inserted, already existing
            updateOrAbort(obj)
        }
    }
}