package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao

import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Views

@Dao
interface ViewsDAO: BaseDAO<Views> {

    @Query("DELETE FROM document_views_table WHERE docId == :docId AND typeS == :type")
    fun deleteCopy(docId: Int, type: String)

    @Query("SELECT * FROM document_views_table")
    fun getViews(): LiveData<List<Views>>

}