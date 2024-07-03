package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.AnaliticSkill
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AnalyticSkillGraphInfo
import java.sql.Date

@Dao
interface AnalyticDAO: BaseDAO<AnaliticSkill> {
    // Получение всей аналитики по всем навыкам за определенный период
    @Query("SELECT * FROM analitic_skill_table WHERE knowId IS NOT NULL " +
            "and (:startData IS NULL or:startData < date) " +
            "and (:endData IS NULL or date < :endData)" +
            " ORDER BY date DESC")
    fun getKnowledgeSkillData(endData: Date?, startData: Date?): LiveData<List<AnaliticSkill>>

    // Получение всей аналитики по всем специальностям за определенный период
    @Query("SELECT * FROM analitic_skill_table WHERE specId IS NOT NULL " +
            "and (:startData IS NULL or:startData < date) " +
            "and (:endData IS NULL or date < :endData)" +
            " ORDER BY date DESC")
    fun getSpecializationSkillData(endData: Date?, startData: Date?): LiveData<List<AnaliticSkill>>

    // Получение всей аналитики по определенному навыку за определенный период
    @Query("SELECT * FROM analitic_skill_table WHERE knowId == :knowId " +
            "and (:startData IS NULL or:startData < date) " +
            "and (:endData IS NULL or date < :endData) " +
            "ORDER BY date DESC")
    fun getKnowledgeSkillByIdData(endData: Date?, startData: Date?, knowId: Int ): LiveData<List<AnaliticSkill>>

    // Получение всей аналитики по определенной специальности за определенный период
    @Query(
        "SELECT * FROM analitic_skill_table WHERE specId == :specId " +
            "and (:startData IS NULL or:startData < date) " +
            "and (:endData IS NULL or date < :endData) " +
            "ORDER BY date DESC")
    fun getSpecializationSkillByIdData(endData: Date?, startData: Date?, specId: Int ): LiveData<List<AnaliticSkill>>

    @Query("SELECT analitic_skill_table.id, analitic_skill_table.specId, analitic_skill_table.knowId," +
            " analitic_skill_table.numUsage, analitic_skill_table.date, analitic_skill_table.respType " +
            "FROM analitic_skill_table JOIN knowledge_table ON knowledge_table.knowId == analitic_skill_table.knowId " +
            "JOIN knowledge_type_cross_ref ON knowledge_table.knowId == knowledge_type_cross_ref.knowId " +
            "WHERE knowledge_type_cross_ref.id == :skillFamilyId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "ORDER BY date DESC")
    fun getKnowledgeSkillBySkillFamilyIdData(endData: Date?, startData: Date?, skillFamilyId: Int ): LiveData<List<AnaliticSkill>>

    @Query("SELECT analitic_skill_table.id, analitic_skill_table.specId, analitic_skill_table.knowId," +
            " analitic_skill_table.numUsage, analitic_skill_table.date, analitic_skill_table.respType FROM analitic_skill_table " +
            "JOIN specialization_table ON specialization_table.specId == analitic_skill_table.specId " +
            "JOIN specialization_type_cross_ref ON specialization_table.specId == specialization_type_cross_ref.specId " +
            "WHERE specialization_type_cross_ref.id == :skillFamilyId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "ORDER BY date DESC")
    fun getSpecializationSkillBySkillFamilyIdData(endData: Date?, startData: Date?, skillFamilyId: Int ): LiveData<List<AnaliticSkill>>

    @Query("SELECT strftime(:param, datetime(date/1000, 'unixepoch')) as date, " +
            "SUM(analitic_skill_table.numUsage) as numUsage, analitic_skill_table.respType as respType, " +
            "knowledge_table.name as skillName FROM analitic_skill_table " +
            "JOIN knowledge_table ON knowledge_table.knowId == analitic_skill_table.knowId " +
            "JOIN knowledge_type_cross_ref ON knowledge_table.knowId == knowledge_type_cross_ref.knowId " +
            "WHERE knowledge_type_cross_ref.id == :skillFamilyId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY strftime(:param, datetime(date/1000, 'unixepoch')), analitic_skill_table.respType, analitic_skill_table.knowId " +
            "ORDER BY date ASC")
    fun getAnalyticKnowInfoBySkillFamilyIdData(endData: Date?, startData: Date?, skillFamilyId: Int, param: String): LiveData<List<AnalyticSkillGraphInfo>>

    @Query("SELECT strftime(:param, datetime(date/1000, 'unixepoch')) as date, " +
            "SUM(analitic_skill_table.numUsage) as numUsage, analitic_skill_table.respType as respType, " +
            "knowledge_table.name as skillName FROM analitic_skill_table " +
            "JOIN knowledge_table ON knowledge_table.knowId == analitic_skill_table.knowId " +
            "WHERE knowledge_table.knowId == :knowId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY strftime(:param, datetime(date/1000, 'unixepoch')), analitic_skill_table.respType, analitic_skill_table.knowId " +
            "ORDER BY date ASC")
    fun getAnalyticKnowInfoBySkillIdData(endData: Date?, startData: Date?, knowId: Int, param: String): LiveData<List<AnalyticSkillGraphInfo>>


    @Query("SELECT SUM(analitic_skill_table.numUsage) as numUsage, " +
            "analitic_skill_table.respType as respType, " +
            "knowledge_table.name as skillName FROM analitic_skill_table " +
            "JOIN knowledge_table ON knowledge_table.knowId == analitic_skill_table.knowId " +
            "JOIN knowledge_type_cross_ref ON knowledge_table.knowId == knowledge_type_cross_ref.knowId " +
            "WHERE knowledge_type_cross_ref.id == :skillFamilyId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY analitic_skill_table.respType, analitic_skill_table.knowId " +
            "ORDER BY numUsage ASC")
    fun getBarAnalyticKnowInfoBySkillFamilyIdData(endData: Date?, startData: Date?, skillFamilyId: Int): LiveData<List<AnalyticSkillGraphInfo>>

    @Query("SELECT SUM(analitic_skill_table.numUsage) as numUsage, " +
            "analitic_skill_table.respType as respType, " +
            "knowledge_table.name as skillName FROM analitic_skill_table " +
            "JOIN knowledge_table ON knowledge_table.knowId == analitic_skill_table.knowId " +
            "WHERE knowledge_table.knowId =:knowId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY analitic_skill_table.respType, analitic_skill_table.knowId " +
            "ORDER BY numUsage ASC")
    fun getBarAnalyticKnowInfoByIdData(endData: Date?, startData: Date?, knowId: Int): LiveData<List<AnalyticSkillGraphInfo>>

    @Query("SELECT strftime(:param, datetime(date/1000, 'unixepoch')) as date, " +
            "SUM(analitic_skill_table.numUsage) as numUsage, analitic_skill_table.respType as respType, " +
            "knowledge_table.name as skillName FROM analitic_skill_table " +
            "JOIN knowledge_table ON knowledge_table.knowId == analitic_skill_table.knowId " +
            "JOIN knowledge_type_cross_ref ON knowledge_table.knowId == knowledge_type_cross_ref.knowId " +
            "WHERE knowledge_type_cross_ref.id == :skillFamilyId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY strftime(:param, datetime(date/1000, 'unixepoch')), analitic_skill_table.respType, analitic_skill_table.knowId " +
            "ORDER BY numUsage ASC")
    fun getAnalyticKnowInfoForHBarBySkillFamilyIdData(endData: Date?, startData: Date?, skillFamilyId: Int, param: String): LiveData<List<AnalyticSkillGraphInfo>>

    @Query("SELECT strftime(:param, datetime(date/1000, 'unixepoch')) as date, " +
            "SUM(analitic_skill_table.numUsage) as numUsage, analitic_skill_table.respType as respType, " +
            "specialization_table.name as skillName FROM analitic_skill_table " +
            "JOIN specialization_table ON specialization_table.specId == analitic_skill_table.specId " +
            "JOIN specialization_type_cross_ref ON specialization_table.specId == specialization_type_cross_ref.specId " +
            "WHERE specialization_type_cross_ref.id == :skillFamilyId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY strftime(:param, datetime(date/1000, 'unixepoch')), analitic_skill_table.respType, analitic_skill_table.specId  " +
            "ORDER BY date ASC")
    fun getAnalyticSpecInfoBySkillFamilyIdData(endData: Date?, startData: Date?, skillFamilyId: Int, param: String): LiveData<List<AnalyticSkillGraphInfo>>
    @Query("SELECT SUM(analitic_skill_table.numUsage) as numUsage, " +
            "analitic_skill_table.respType as respType, " +
            "specialization_table.name as skillName FROM analitic_skill_table " +
            "JOIN specialization_table ON specialization_table.specId == analitic_skill_table.specId " +
            "JOIN specialization_type_cross_ref ON specialization_table.specId == specialization_type_cross_ref.specId " +
            "WHERE specialization_type_cross_ref.id == :skillFamilyId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY analitic_skill_table.respType, analitic_skill_table.specId  " +
            "ORDER BY numUsage ASC")
    fun getBarAnalyticSpecInfoBySkillFamilyIdData(endData: Date?, startData: Date?, skillFamilyId: Int): LiveData<List<AnalyticSkillGraphInfo>>

    @Query("SELECT SUM(analitic_skill_table.numUsage) as numUsage, " +
            "analitic_skill_table.respType as respType, " +
            "specialization_table.name as skillName FROM analitic_skill_table " +
            "JOIN specialization_table ON specialization_table.specId == analitic_skill_table.specId " +
            "JOIN specialization_type_cross_ref ON specialization_table.specId == specialization_type_cross_ref.specId " +
            "WHERE specialization_table.specId == :specId and (:startData IS NULL or:startData < date) and (:endData IS NULL or date < :endData) " +
            "GROUP BY analitic_skill_table.respType, analitic_skill_table.specId  " +
            "ORDER BY numUsage ASC")
    fun getBarAnalyticSpecInfoByIdData(endData: Date?, startData: Date?, specId: Int): LiveData<List<AnalyticSkillGraphInfo>>

}