package ru.kondrashen.diplomappv20.domain

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.kondrashen.diplomappv20.repository.data_class.AnaliticSkill
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.SkillType
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AnalyticSkillGraphInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentAnalysisInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo
import ru.kondrashen.diplomappv20.repository.db.WorkSearcherDB
import ru.kondrashen.diplomappv20.repository.repositories.DocDependenciesRepository
import ru.kondrashen.diplomappv20.repository.repositories.ExperienceRepository
import ru.kondrashen.diplomappv20.repository.repositories.KnowledgeRepository
import ru.kondrashen.diplomappv20.repository.repositories.SkillTypeRepository
import ru.kondrashen.diplomappv20.repository.repositories.SpecializationToEducationRepository
import java.sql.Date

class ExtraInfoPageViewModel(application: Application): AndroidViewModel(application) {
    private val dependenceRep: DocDependenciesRepository
    private val knowledgeRep: KnowledgeRepository
    private val experienceRep: ExperienceRepository
    private val specToEduRep: SpecializationToEducationRepository
    private val skillTypeRep: SkillTypeRepository
    private var trigger = MutableLiveData<List<String>>()
    init {
        val docDao = WorkSearcherDB.getDatabase(application).docDao()
        val specDao = WorkSearcherDB.getDatabase(application).specDao()
        val eduDao = WorkSearcherDB.getDatabase(application).eduDao()
        val expDao = WorkSearcherDB.getDatabase(application).expDao()
        val expTimeDao = WorkSearcherDB.getDatabase(application).expTimeDao()
        val docDepDao = WorkSearcherDB.getDatabase(application).docDependDao()
        val docViewDao = WorkSearcherDB.getDatabase(application).docViewDao()
        val knowDao = WorkSearcherDB.getDatabase(application).knowDao()
        val skillTypeDAO = WorkSearcherDB.getDatabase(application).skillTypeDao()
        val analyticDao = WorkSearcherDB.getDatabase(application).analyticDao()
        dependenceRep = DocDependenciesRepository(docDao, docDepDao, docViewDao)
        specToEduRep = SpecializationToEducationRepository(specDao, eduDao, analyticDao)
        knowledgeRep = KnowledgeRepository(knowDao, analyticDao)
        skillTypeRep = SkillTypeRepository(skillTypeDAO, knowDao, specDao)
        experienceRep = ExperienceRepository(expDao, expTimeDao)

    }
    fun getDocumentInfoRoom(id: Int): LiveData<DocumentInfoWithKnowledge> {
        return dependenceRep.getDocumentById(id)
    }
    fun getDocumentDependenceRoom(id: Int): LiveData<List<DocDependenceFullInfo>> {
        return dependenceRep.getDependenceInfoByDocumentId(id)
    }
    fun getDocumentDependenceNamesRoom(id: Int): LiveData<List<String>> {
        return dependenceRep.getDependenceNameByDocId(id)
    }
    fun getDocumentExperiencesRoom(id: Int): LiveData<List<ExperienceInfo>> {
        return experienceRep.getExperienceFullInfoByDocId(id)
    }
    fun getDocumentExperienceRoom(id: Int): LiveData<List<String>> {
        return experienceRep.getExperienceTimesByDocId(id)
    }
    fun getDependenceById(dependId: Int): LiveData<DocDependenceFullInfo> {
        return dependenceRep.getDependenceById(dependId)
    }
    fun getSpecializationId(name: String): LiveData<Int> {
        return specToEduRep.getSpecializationId(name)
    }
    fun getSpecializationNames(): LiveData<List<String>> {
        return specToEduRep.getSpecializationNameList()
    }
    fun getExperienceTimeNameList(): LiveData<List<String>> {
        return experienceRep.getExperienceTimeNameList()
    }
    fun getExperienceTimeId(name: String): LiveData<Int> {
        return experienceRep.getExperienceTimeId(name)
    }
    fun getExperienceById(id: Int): LiveData<ExperienceInfo> {
        return experienceRep.getExperienceFullInfoByExpId(id)
    }
    fun getAppExpById(id: Int): LiveData<UserExperience> {
        return experienceRep.getAppExpInfoByExpId(id)
    }
    fun getExperienceTimeNameId(name: String): LiveData<Int> {
        return experienceRep.getExperienceTimeId(name)
    }
    fun getUserAnalysisDocFromRoom(userId: Int): LiveData<List<DocumentAnalysisInfo>>{
        return dependenceRep.getDocsAnalysisInfoByUserId(userId)
    }
    fun getEducationNamesBySpecName(name: String): LiveData<List<String>> {
        return specToEduRep.getEducationsBySpecName(name)
    }
    fun getEducationId(name: String): LiveData<Int> {
        return specToEduRep.getEducationId(name)
    }
    fun getSkillTypesFromServ(mod: String){
        skillTypeRep.getSkillTypesFromServ(mod)
    }
    fun getSkillTypeNamesFromRoom(): LiveData<List<String>>{
        return skillTypeRep.getSkillTypeNamesFromRoom()
    }
    fun getSkillTypeIdByNameFromRoom(name: String): LiveData<Int>{
        return skillTypeRep.getSkillTypeIdByNameFromRoom(name)
    }
    fun getSkillTypesFromRoom(): LiveData<List<SkillType>>{
        return skillTypeRep.getSkillTypesFromRoom()
    }
    fun getSkillIdBySkillNameFromRoom(type: String, name: String): LiveData<Int>{
        return when (type) {
            "specialization" -> specToEduRep.getSpecializationIdByName(name)
            "knowledge" -> knowledgeRep.getKnowledgeIdByName(name)
            else -> knowledgeRep.getKnowledgeIdByName(name)
        }
    }
    fun getSkillsNamesBySkillTypeAndClass(type: String, skillTypeName: String): LiveData<List<String>> {
        return when (type) {
            "specialization" -> specToEduRep.getSpecializationNameListByTypeName(skillTypeName)
            "knowledge" -> knowledgeRep.getKnowledgeNameListByTypeName(skillTypeName)
            else -> knowledgeRep.getKnowledgeNameListByTypeName(skillTypeName)
        }
    }
    fun getKnowledgeBySkillClass(skillTypeName: String): LiveData<List<Knowledge>> {
        return knowledgeRep.getKnowledgeListByTypeName(skillTypeName)

    }
    fun getKnowledgeNamesBySkillTypeAndClass(type: String, skillTypeName: String): LiveData<List<String>> {
        return knowledgeRep.getKnowledgeNameListByTypeName(skillTypeName)
    }
    fun getStartSkillDataFromServ(mod: String){
        knowledgeRep.getKnowledgeFromServ(mod)
        specToEduRep.getSpecializationModedFromServ(mod)
    }
    fun getSkillItemsBySkillTypeAndClassFromServ(type: String, skillTypeId: Int){
        when (type) {
            "specialization" -> specToEduRep.getSpecializationBySkillTypeIdFromServ(skillTypeId)
            "knowledge" -> knowledgeRep.getKnowledgeBySkillTypeIdFromServ(skillTypeId)
        }
    }

    fun getSkillDataFromServ(mode: String, startDate: Date, endDate: Date?, skillId: Int?, skillFamilyId: Int?, skillType: String): LiveData<String> {
        return knowledgeRep.getAnalyticDataFromServ(mode, startDate, endDate, skillId, skillFamilyId, skillType)
    }
    fun getSkillDataFromRoom(mode: String, startDate: Date?, endDate: Date?, skillId: Int?, skillFamilyId: Int?, skillType: String): LiveData<List<AnaliticSkill>> {
        if (skillType == "knowledge")
            return knowledgeRep.getKnowledgeAnalyticInfoFromRoom(startDate, endDate, skillId, skillFamilyId)
        else
            return specToEduRep.getSpecializationAnalyticInfoFromRoom(startDate, endDate, skillId, skillFamilyId)
    }
    fun getAnalyticDataFromRoom(mode: String, startDate: Date?, endDate: Date?, skillId: Int?, skillFamilyId: Int, skillType: String): LiveData<List<AnalyticSkillGraphInfo>> {
        var pref =when(mode){
                "year" -> "%Y"
                "month" -> "%Y-%m"
                "day" -> "%Y-%m-%d"
                else ->"%Y-%m-%d"
        }
        if (skillType == "knowledge")
            return knowledgeRep.getFullKnowAnalyticInfoFromRoom(startDate, endDate, skillId, skillFamilyId, pref)
        else
            return specToEduRep.getFullSpecAnalyticInfoFromRoom(startDate, endDate, skillId, skillFamilyId, pref)
    }

    fun getBarAnalyticDataFromRoom(startDate: Date?, endDate: Date?, skillId: Int?, skillFamilyId: Int, skillType: String): LiveData<List<AnalyticSkillGraphInfo>> {
        if (skillType == "knowledge")
            return knowledgeRep.getBarKnowAnalyticInfoFromRoom(startDate, endDate, skillId, skillFamilyId)
        else
            return specToEduRep.getBarSpecAnalyticInfoFromRoom(startDate, endDate, skillId, skillFamilyId)
    }

    fun getAnalyticsNames(type: String, skillFamilyId: Int): LiveData<List<String>>{
        return when (type) {
            "specialization" -> specToEduRep.getSpecSkillNamesById(skillFamilyId)
            "knowledge" -> knowledgeRep.getKnowSkillNamesById(skillFamilyId)
            else -> knowledgeRep.getKnowSkillNamesById(skillFamilyId)
        }
    }

    fun getKnowledge(): LiveData<List<Knowledge>>{
        return knowledgeRep.getKnowledgeInfo()
    }
    fun getKnowledgeByIdsRoom(iDs: List<Int>, mod: String, skillTypeName: String?): LiveData<List<Knowledge>> {
        return knowledgeRep.getKnowledgeByIDsInfo( iDs, mod, skillTypeName)
    }

    fun getAnaliticInfoFromRoom(iDs: List<Int>, mod: String, skillTypeName: String): LiveData<List<Knowledge>> {
        return knowledgeRep.getKnowledgeByIDsInfo( iDs, mod, skillTypeName)
    }

}