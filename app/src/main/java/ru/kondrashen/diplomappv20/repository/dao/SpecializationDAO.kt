package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToEducationCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToUserCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationTypeCrossRef

@Dao
interface SpecializationDAO: BaseDAO<Specialization> {


    @Query("SELECT * FROM specialization_table ORDER BY name DESC")
    fun getSpecs(): LiveData<List<Specialization>>

    @Query("SELECT name FROM specialization_table ORDER BY name DESC")
    fun getSpecsName(): LiveData<List<String>>

    @Query("SELECT specialization_table.name FROM specialization_table " +
            "JOIN specialization_type_cross_ref " +
            "ON specialization_type_cross_ref.specId == specialization_table.specId " +
            "WHERE specialization_type_cross_ref.id = :skillTypeId " +
            "ORDER BY specialization_table.name DESC")
    fun getSpecSkillNamesById(skillTypeId: Int): LiveData<List<String>>

    @Query("SELECT specialization_table.name FROM specialization_table " +
            "JOIN specialization_type_cross_ref " +
            "ON specialization_table.specId == specialization_type_cross_ref.specId " +
            "JOIN skill_type_table ON specialization_type_cross_ref.id == skill_type_table.id " +
            "WHERE skill_type_table.name == :skillTypeName " +
            "ORDER BY specialization_table.name DESC")
    fun getSpecsNameByTypeName(skillTypeName: String): LiveData<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSpecTyped(item: SpecializationTypeCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSpecsTyped(vararg item: SpecializationTypeCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSpecToUser(item: SpecializationToUserCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSpecToEdu(item: SpecializationToEducationCrossRef)

    @Query("SELECT specId FROM specialization_table WHERE name == :name")
    fun getSpecializationId(name: String): LiveData<Int>
    

    @Query("SELECT name FROM specialization_table WHERE name LIKE '%' || :text || '%' ORDER BY name DESC")
    fun getSpecsByFilter(text: String): LiveData<List<String>>
}