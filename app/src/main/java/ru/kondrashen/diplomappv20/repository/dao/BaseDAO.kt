package ru.kondrashen.diplomappv20.repository.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

interface BaseDAO<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItems(vararg item: T)

    @Update
    suspend fun updateItem(item: T)

    @Delete
    suspend fun deleteItem(item: T)

    @Query("DELETE FROM document_table")
    fun deleteDocData()

    @Query("DELETE FROM document_views_table")
    fun deleteViewsData()

    @Query("DELETE FROM dependence_table")
    fun deleteDependenceData()

    @Query("DELETE FROM comment_table")
    fun deleteCommData()

    @Query("DELETE FROM education_table")
    fun deleteEduData()

    @Query("DELETE FROM specialization_table")
    fun deleteSpecData()

    @Query("DELETE FROM spec_to_edu_table")
    fun deleteSpecToEduData()

    @Query("DELETE FROM knowledge_table")
    fun deleteKnowData()

    @Query("DELETE FROM user_table")
    fun deleteUserData()

    @Query("DELETE FROM document_response_table")
    fun deleteRespData()

    @Query("DELETE FROM knowledgetodocumentcrossref")
    fun deleteKnowToDocData()

    @Query("DELETE FROM dependencies_to_document")
    fun deleteDependToDocData()

    @Query("DELETE FROM analitic_skill_table")
    fun deleteAnalyticData()

    @Query("DELETE FROM skill_type_table")
    fun deleteSkillTypeData()


    @Transaction
    fun deleteAllData(){
        deleteEduData()
        deleteSpecData()
        deleteCommData()
        deleteAnalyticData()
        deleteDependenceData()
        deleteDependToDocData()
        deleteSkillTypeData()
        deleteKnowToDocData()
        deleteDocData()
        deleteViewsData()
        deleteRespData()
        deleteUserData()
        deleteKnowData()
    }

}