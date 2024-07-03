package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.User

@Dao
interface EducationDAO: BaseDAO<Education> {
    @Query("SELECT * FROM education_table ORDER BY name DESC")
    fun getEducations(): LiveData<List<Education>>

    @Query("SELECT id FROM education_table WHERE name == :name")
    fun getEducationId(name: String): LiveData<Int>

    @Query("SELECT education_table.name FROM education_table JOIN spec_to_edu_table ON spec_to_edu_table.id = education_table.id JOIN specialization_table ON specialization_table.specId = spec_to_edu_table.specId WHERE specialization_table.name = :name")
    fun getEducationsBySpecName(name: String): LiveData<List<String>>
}