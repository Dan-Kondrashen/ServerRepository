package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.relationship.DependenciesToDocumentCrossRef

@Dao
interface DependenciesDAO: BaseDAO<DocDependencies> {
    @Query("SELECT * FROM dependence_table")
    fun getDocuments(): LiveData<List<DocDependencies>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDocDep(item: DependenciesToDocumentCrossRef)

    @Query("DELETE FROM dependence_table WHERE id ==:dependId")
    fun deleteDepend(dependId: Int)

    @Query("SELECT * FROM dependence_table JOIN dependencies_to_document ON dependencies_to_document.id =dependence_table.id JOIN document_table ON document_table.docId = dependencies_to_document.docId WHERE document_table.docId == :id ORDER BY specId DESC")
    fun getDependenciesInfoByDocId(id: Int): LiveData<List<DocDependenceFullInfo>>

    @Query("SELECT specialization_table.name FROM specialization_table " +
            "JOIN dependence_table ON dependence_table.specId == specialization_table.specId " +
            "JOIN dependencies_to_document ON dependencies_to_document.id == dependence_table.id " +
            "WHERE dependencies_to_document.docId == :id")
    fun getDependenciesNameByDocId(id: Int): LiveData<List<String>>

    @Query("SELECT * FROM dependence_table WHERE id == :id")
    fun getDependenceById(id: Int): LiveData<DocDependenceFullInfo>

    @Query("SELECT * FROM dependence_table WHERE dependence_table.userId == :id ORDER BY specId DESC ")
    fun getDependenciesInfoByUserId(id: Int): LiveData<List<DocDependenceFullInfo>>

    @Query("SELECT * FROM dependence_table WHERE dependence_table.userId == :id AND dependence_table.id IN (:depIds) ORDER BY specId DESC ")
    fun getDependenciesInfoWithIdByUserId(id: Int, depIds: List<Int>): LiveData<List<DocDependenceFullInfo>>

    @Query("SELECT * FROM dependence_table WHERE dependence_table.userId == :id AND dependence_table.id NOT IN (:depIds) ORDER BY specId DESC ")
    fun getDependenciesInfoWithoutIdByUserId(id: Int, depIds: List<Int>): LiveData<List<DocDependenceFullInfo>>
}