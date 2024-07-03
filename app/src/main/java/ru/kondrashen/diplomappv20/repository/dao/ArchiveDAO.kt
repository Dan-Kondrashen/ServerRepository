package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.Role

@Dao
interface ArchiveDAO: BaseDAO<Archive> {
    @Query("SELECT * FROM archive_table WHERE userId = :id ORDER BY name DESC")
    fun getUserArchives(id: Int): LiveData<List<Archive>>

    @Query("SELECT * FROM archive_table WHERE userId = :id ORDER BY name DESC")
    fun getUserArchivesStatic(id: Int): List<Archive>

    @Query("SELECT * FROM archive_table WHERE archive_table.id = :id")
    fun getCurArchiveNameById(id: Int): LiveData<Archive>

    @Query("DELETE FROM archive_table WHERE id = :id")
    fun deleteArchiveById(id: Int)

    @Query("UPDATE archive_table SET name = :name WHERE id = :id")
    fun updateArchiveById(id: Int, name: String)


}