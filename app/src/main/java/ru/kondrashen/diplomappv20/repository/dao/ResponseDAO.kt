package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse

@Dao
interface ResponseDAO: BaseDAO<DocResponse> {
    @Query("SELECT * FROM document_response_table")
    fun getResponses(): LiveData<List<DocResponse>>

    @Query("SELECT type FROM document_response_table WHERE userId == :userId AND docId ==:docId")
    fun getResponsesNameByDocAndUser(userId: Int, docId: Int): LiveData<List<String>>

    @Query("SELECT document_response_table.type FROM document_response_table WHERE userId == :userId AND docId ==:docId AND type NOT IN (:types)")
    fun getResponsesIdByDocAndUserWithoutNames(userId: Int, docId: Int, types: List<String>): LiveData<List<String>>

    @Query("SELECT * FROM document_response_table WHERE document_response_table.id == :id ORDER BY id DESC ")
    fun getResponsesById(id: Int): LiveData<List<DocResponse>>

    @Query("DELETE FROM document_response_table WHERE document_response_table.type == :type")
    fun deleteByRespType(type: String)

    @Query("UPDATE document_response_table SET id = :respId WHERE type == :type AND docId == :docId AND userId = :userId")
    fun updateRespByParam(respId: Int,  userId: Int, type: String, docId: Int)

}