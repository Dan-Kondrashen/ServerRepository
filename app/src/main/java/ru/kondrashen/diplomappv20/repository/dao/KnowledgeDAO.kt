package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeTypeCrossRef

@Dao
interface KnowledgeDAO: BaseDAO<Knowledge> {
    @Query("SELECT * FROM knowledge_table ORDER BY name DESC")
    fun getKnowledge(): LiveData<List<Knowledge>>

    @Query("SELECT knowId FROM knowledge_table WHERE name == :name")
    fun getKnowledgeId(name: String): LiveData<Int>



    @Query("SELECT knowledge_table.name FROM knowledge_table " +
            "JOIN knowledge_type_cross_ref " +
            "ON knowledge_type_cross_ref.knowId == knowledge_table.knowId " +
            "WHERE knowledge_type_cross_ref.id= :skillTypeId " +
            "ORDER BY knowledge_table.name DESC")
    fun getSkillKnowNames(skillTypeId: Int): LiveData<List<String>>

    @Query("SELECT * FROM knowledge_table WHERE knowId IN (:knowIDs) ORDER BY name DESC")
    fun getKnowledgeWithIDs(knowIDs: List<Int>): LiveData<List<Knowledge>>

    @Query("SELECT knowledge_table.name FROM knowledge_table " +
            "JOIN knowledge_type_cross_ref " +
            "ON knowledge_table.knowId == knowledge_type_cross_ref.knowId " +
            "JOIN skill_type_table ON knowledge_type_cross_ref.id == skill_type_table.id " +
            "WHERE skill_type_table.name == :skillTypeName " +
            "ORDER BY knowledge_table.name DESC")
    fun getKnowledgeNamesBySkillTypeName(skillTypeName: String): LiveData<List<String>>

    @Query("SELECT knowledge_table.knowId, knowledge_table.name, knowledge_table.description FROM knowledge_table " +
            "JOIN knowledge_type_cross_ref " +
            "ON knowledge_table.knowId == knowledge_type_cross_ref.knowId " +
            "JOIN skill_type_table ON knowledge_type_cross_ref.id == skill_type_table.id " +
            "WHERE skill_type_table.name == :skillTypeName " +
            "ORDER BY knowledge_table.name DESC")
    fun getKnowledgeBySkillTypeName(skillTypeName: String): LiveData<List<Knowledge>>

    @Query("SELECT knowledge_table.knowId, knowledge_table.name, knowledge_table.description FROM knowledge_table " +
            "JOIN knowledge_type_cross_ref " +
            "ON knowledge_table.knowId == knowledge_type_cross_ref.knowId " +
            "JOIN skill_type_table ON knowledge_type_cross_ref.id == skill_type_table.id " +
            "WHERE skill_type_table.name == :skillTypeName AND knowledge_table.knowId NOT IN (:knowIDs) " +
            "ORDER BY knowledge_table.name DESC")
    fun getKnowledgeWithoutIDs(knowIDs: List<Int>, skillTypeName: String): LiveData<List<Knowledge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addKnowToDoc(item: KnowledgeToDocumentCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addKnowsType(item: KnowledgeTypeCrossRef)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addKnowsTyped(vararg item: KnowledgeTypeCrossRef)
}