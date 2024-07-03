package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.SkillType

@Dao
interface SkillTypeDAO: BaseDAO<SkillType> {
    @Query("SELECT * FROM skill_type_table")
    fun getSkillTypes(): LiveData<List<SkillType>>

    @Query("SELECT name FROM skill_type_table ORDER BY name")
    fun getSkillTypeNames(): LiveData<List<String>>

    @Query("SELECT id FROM skill_type_table WHERE name == :name")
    fun getSkillTypeIdByName(name: String): LiveData<Int>

}