package de.psdev.devdrawer.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WidgetDao : BaseDao<Widget>() {

    @Query("SELECT * FROM widgets")
    abstract suspend fun findAll(): List<Widget>

    @Query("SELECT * FROM widgets")
    abstract fun findAllBlocking(): List<Widget>

    @Query("SELECT * FROM widgets")
    abstract fun findAllFlow(): Flow<List<Widget>>

    @Query("SELECT * FROM widgets WHERE id = :id")
    abstract suspend fun findById(id: Int): Widget?

    @Query("DELETE FROM widgets WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>)

}
