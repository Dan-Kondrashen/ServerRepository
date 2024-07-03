package ru.kondrashen.diplomappv20.repository.repositories

import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.AnalyticDAO
import ru.kondrashen.diplomappv20.repository.dao.KnowledgeDAO
import ru.kondrashen.diplomappv20.repository.data_class.AnaliticSkill
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AnalyticFilter
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AnalyticSkillGraphInfo
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeTypeCrossRef
import java.sql.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class KnowledgeRepository(private val knowDAO: KnowledgeDAO, private val analyticDAO: AnalyticDAO): CoroutineScope {
    private lateinit var stringResp: MutableLiveData<String>
    private val analyticAPI = APIFactory.analyticApi
    private val job = SupervisorJob()
    private val knowAPI = APIFactory.knowledgeApi
    companion object{
        const val KNOW_REP = "know rep"
    }
    override val coroutineContext: CoroutineContext
        get() = job

    fun getKnowledgeInfo(): LiveData<List<Knowledge>> {
        return knowDAO.getKnowledge()
    }
    fun getKnowledgeNameListByTypeName(skillTypeName: String): LiveData<List<String>> {
        return knowDAO.getKnowledgeNamesBySkillTypeName(skillTypeName)
    }
    fun getKnowledgeListByTypeName(skillTypeName: String): LiveData<List<Knowledge>> {
        return knowDAO.getKnowledgeBySkillTypeName(skillTypeName)
    }

    fun getKnowledgeIdByName(name: String): LiveData<Int> {
        return knowDAO.getKnowledgeId(name)
    }

    fun getKnowledgeByIDsInfo(iDsList: List<Int>,mod: String, skillTypeName: String?): LiveData<List<Knowledge>> {
        return when(mod){
            "in" -> knowDAO.getKnowledgeWithIDs(iDsList)
            else -> knowDAO.getKnowledgeWithoutIDs(iDsList, skillTypeName?: "")
        }
    }
    fun getKnowledgeBySkillTypeIdFromServ(skillTypeId: Int) {
        launch(Dispatchers.IO) {
            try{
                val resp = knowAPI.getKnowledgeBySkillTypeAsync(skillTypeId)
                if (resp.isSuccessful){
                    val know = resp.body()
                    know?.let { knowledge->
                        knowDAO.addItems(*knowledge.toTypedArray())
                        var result = knowledge.map { KnowledgeTypeCrossRef(skillTypeId, it.knowId) }
                        knowDAO.addKnowsTyped(*result.toTypedArray())
                    }

                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    fun getKnowledgeFromServ(mod: String) {
        launch(Dispatchers.IO) {
            try{
                val resp = knowAPI.getKnowledgeAsync(mod)
                if (resp.isSuccessful){
                    val know = resp.body()
                    know?.let { knowledge->
                        println(knowledge +"result")
                        knowDAO.addItems(*knowledge.toTypedArray())
                    }

                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }



    fun getKnowledgeAnalyticInfoFromRoom(startDate: Date?, endDate: Date?, knowId: Int?, skillFamilyId: Int?): LiveData<List<AnaliticSkill>> {
        return if (knowId != null)
            analyticDAO.getKnowledgeSkillByIdData(endDate, startDate, knowId)
        else if (skillFamilyId != null)
            analyticDAO.getKnowledgeSkillBySkillFamilyIdData(endDate, startDate, skillFamilyId)
        else
            analyticDAO.getKnowledgeSkillData(startDate, endDate)
    }
    fun getFullKnowAnalyticInfoFromRoom(startDate: Date?, endDate: Date?, knowId: Int?, skillFamilyId: Int, param: String): LiveData<List<AnalyticSkillGraphInfo>> {
       knowId?.let {
           println(knowId)
           return analyticDAO.getAnalyticKnowInfoBySkillIdData(endDate, startDate, it, param)
       }?: run {
           return analyticDAO.getAnalyticKnowInfoBySkillFamilyIdData(endDate, startDate, skillFamilyId, param)
       }

    }
    fun getBarKnowAnalyticInfoFromRoom(startDate: Date?, endDate: Date?, knowId: Int?, skillFamilyId: Int): LiveData<List<AnalyticSkillGraphInfo>> {
        knowId?.let {
            return analyticDAO.getBarAnalyticKnowInfoByIdData(endDate, startDate, knowId)
        }?: run{
            return analyticDAO.getBarAnalyticKnowInfoBySkillFamilyIdData(endDate, startDate, skillFamilyId)
        }
    }
    fun getKnowSkillNamesById(skillTypeId: Int): LiveData<List<String>>{
        return knowDAO.getSkillKnowNames(skillTypeId)
    }
    fun getAnalyticDataFromServ(mode: String, startDate: Date?, endDate: Date?, skillId: Int?, skillFamilyId: Int?, skillType: String): LiveData<String> {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO){
            try{
                val resp= analyticAPI.getSkillAnalysisAsync(mode, AnalyticFilter(
                    format.format(endDate),
                    format.format(startDate),
                    skillId,
                    skillType = skillType,
                    skillFamilyId = skillFamilyId))
                if (resp.isSuccessful) {
                    val jsonObject = resp.body()
                    stringResp.postValue("success")
                    println(jsonObject)
                    jsonObject?.let {
                        for (i in jsonObject) {
                            val analytic =AnaliticSkill(id = i.get("id").asInt,
                                date =Date(format.parse(i.get("date").asString).time),
                                numUsage = i.get("numUsage").asInt,
                                respType = i.get("respType").asString,
                                knowId = if (i.get("knowId").isJsonNull) null else i.get("knowId").asInt,
                                specId = if (i.get("specId").isJsonNull) null else i.get("specId").asInt
                                )
                            analyticDAO.addItem(analytic)
                        }
                    }
                }
                else {
                    Log.i(KNOW_REP, "Запрос на получение аналитики навыков провалился")
                    stringResp.postValue("Server Response Code ${resp.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("ошибка при аналитике")
                stringResp.postValue("No connection to server")
            }
        }
        return stringResp
    }
}