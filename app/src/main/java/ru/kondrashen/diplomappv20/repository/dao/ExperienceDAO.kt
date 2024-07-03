package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.ExperienceTime
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo
import ru.kondrashen.diplomappv20.repository.data_class.relationship.ExperienceToDocumentCrossRef

@Dao
interface ExperienceDAO: BaseDAO<Experience> {
    @Query("SELECT * FROM exp_table ORDER BY experience DESC")
    fun getExperience(): LiveData<List<Experience>>

    @Query("SELECT * FROM level_exp_table WHERE userId == :userId ORDER BY points DESC")
    fun getAppExpList(userId: Int): LiveData<List<UserExperience>>

    @Query("SELECT * FROM level_exp_table WHERE id == :expId")
    fun getAppExpInfoById(expId: Int): LiveData<UserExperience>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAppExpItems(vararg item: UserExperience)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAppExpItem(item: UserExperience)

    @Query("DELETE FROM exp_table WHERE expId ==:expId")
    fun deleteExperience(expId: Int)

    @Query("DELETE FROM level_exp_table WHERE id ==:expId")
    fun deleteAppExp(expId: Int)

    @Query("SELECT expId, exp_time_table.experienceTime, role, experience, place, userId, documentScanId FROM exp_table JOIN exp_time_table ON expTimeId == exp_time_table.id WHERE userId = :userId ORDER BY experience DESC")
    fun getExperienceFullInfoByUserId(userId: Int): LiveData<List<ExperienceInfo>>

    @Query("SELECT exp_table.expId, exp_time_table.experienceTime, role, experience, place, userId, documentScanId " +
            "FROM exp_table " +
            "JOIN exp_time_table ON expTimeId == exp_time_table.id " +
            "JOIN ExperienceToDocumentCrossRef ON ExperienceToDocumentCrossRef.expId = exp_table.expId " +
            "WHERE  ExperienceToDocumentCrossRef.docId = :docId ORDER BY experience DESC")
    fun getExperienceFullInfoByDocId(docId: Int): LiveData<List<ExperienceInfo>>
    @Query("SELECT exp_time_table.experienceTime " +
            "FROM exp_table " +
            "JOIN exp_time_table ON expTimeId == exp_time_table.id " +
            "JOIN ExperienceToDocumentCrossRef ON ExperienceToDocumentCrossRef.expId = exp_table.expId " +
            "WHERE  ExperienceToDocumentCrossRef.docId = :docId ")
    fun getExperienceTimeByDocId(docId: Int): LiveData<List<String>>

    @Query("SELECT expId, exp_time_table.experienceTime, role, experience, place, userId, documentScanId FROM exp_table JOIN exp_time_table ON expTimeId == exp_time_table.id WHERE expId = :expId")
    fun getExperienceFullInfoByExpId(expId: Int): LiveData<ExperienceInfo>

    @Query("SELECT expId, exp_time_table.experienceTime, role, experience, place, userId, documentScanId FROM exp_table JOIN exp_time_table ON expTimeId == exp_time_table.id WHERE userId = :userId AND expId IN (:expListId) ORDER BY experience DESC")
    fun getExperienceInfoWithIdsByUserId(userId: Int, expListId: List<Int>): LiveData<List<ExperienceInfo>>

    @Query("SELECT expId, exp_time_table.experienceTime, role, experience, place, userId, documentScanId FROM exp_table JOIN exp_time_table ON expTimeId == exp_time_table.id WHERE userId = :userId AND expId NOT IN (:expListId) ORDER BY experience DESC")
    fun getExperienceInfoWithoutIdsByUserId(userId: Int, expListId: List<Int>): LiveData<List<ExperienceInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addExpTime(experienceTime: ExperienceTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addExpToDoc(expToDoc: ExperienceToDocumentCrossRef)

}