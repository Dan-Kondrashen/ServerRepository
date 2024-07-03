package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

import ru.kondrashen.diplomappv20.repository.data_class.ExperienceTime


@Dao
interface ExperienceTimeDAO: BaseDAO<ExperienceTime> {
    @Query("SELECT * FROM exp_time_table ORDER BY experienceTime DESC")
    fun getAllExperienceTime(): LiveData<List<ExperienceTime>>

    @Query("SELECT experienceTime FROM exp_time_table ORDER BY experienceTime DESC")
    fun getExperienceTimeNames(): LiveData<List<String>>

    @Query("SELECT id FROM exp_time_table WHERE experienceTime ==:name")
    fun getExperienceTimeId(name: String): LiveData<Int>
}