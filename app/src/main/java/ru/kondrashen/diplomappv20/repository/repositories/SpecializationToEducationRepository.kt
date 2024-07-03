package ru.kondrashen.diplomappv20.repository.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.AnalyticDAO
import ru.kondrashen.diplomappv20.repository.dao.EducationDAO
import ru.kondrashen.diplomappv20.repository.dao.SpecializationDAO
import ru.kondrashen.diplomappv20.repository.data_class.AnaliticSkill
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AnalyticSkillGraphInfo
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToEducationCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationTypeCrossRef
import java.sql.Date
import kotlin.coroutines.CoroutineContext

class SpecializationToEducationRepository(private val specDAO: SpecializationDAO, private val eduDAO: EducationDAO, private val analyticDAO: AnalyticDAO): CoroutineScope {
    private lateinit var stringResp: MutableLiveData<String>
    private val specToEduAPI = APIFactory.specToEduApi
    private val specAPI = APIFactory.specApi
    private val dependAPI = APIFactory.dependApi
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job
    fun getSpecializationId(name: String): LiveData<Int> {
        return specDAO.getSpecializationId(name)
    }
    fun getSpecSkillNamesById(skillTypeId: Int): LiveData<List<String>>{
        return specDAO.getSpecSkillNamesById(skillTypeId)
    }

    fun getSpecializationAnalyticInfoFromRoom(startDate: Date?, endDate: Date?, specId: Int?, skillFamilyId: Int?): LiveData<List<AnaliticSkill>> {
        return if (specId != null)
            analyticDAO.getSpecializationSkillByIdData(endDate, startDate, specId)
        else if (skillFamilyId != null)
            analyticDAO.getSpecializationSkillBySkillFamilyIdData(endDate, startDate, skillFamilyId)
        else
            analyticDAO.getSpecializationSkillData(startDate, endDate)
    }
    fun getFullSpecAnalyticInfoFromRoom(startDate: Date?, endDate: Date?, knowId: Int?, skillFamilyId: Int, param: String): LiveData<List<AnalyticSkillGraphInfo>> {
        return analyticDAO.getAnalyticSpecInfoBySkillFamilyIdData(endDate, startDate, skillFamilyId, param)
    }

    fun getBarSpecAnalyticInfoFromRoom(startDate: Date?, endDate: Date?, specId: Int?, skillFamilyId: Int): LiveData<List<AnalyticSkillGraphInfo>> {
        specId?.let {
            return analyticDAO.getBarAnalyticSpecInfoByIdData(endDate, startDate, specId)
        }?: run{
            return analyticDAO.getBarAnalyticSpecInfoBySkillFamilyIdData(endDate, startDate, skillFamilyId)
        }
    }


    fun postSpecToEduFileToServ(token: String?, userId: Int, addFile: MultipartBody.Part?,
                          name: RequestBody?, specId: RequestBody,
                          eduId: RequestBody
    ): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try{
                val resp= dependAPI.postDependenciesAsync(resultToken, userId, name, specId, eduId, addFile)
                if (resp.isSuccessful) {
                    val jsonObject = resp.body()
                    jsonObject?.get("status")?.let {
                        val result = it.asString
                        stringResp.postValue(result)
                    }
                }
                else
                    stringResp.postValue("Server Response Code ${resp.code()}")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("No connection to server")
            }
        }
        return  stringResp
    }

    fun putSpecToEduFileToServ(token: String?,
                               userId: Int,
                               dependId: Int,
                               addFile: MultipartBody.Part,
                               name: RequestBody,
                               specId: RequestBody,
                               eduId: RequestBody): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try{
                val resp= dependAPI.putDependenciesFileAuth(resultToken, userId, dependId, name, specId, eduId, addFile)
                if (resp.isSuccessful) {
                    val jsonObject = resp.body()
                    jsonObject?.get("status")?.let {
                        val result = it.asString
                        stringResp.postValue(result)
                    }
                }
                else
                    stringResp.postValue("Server Response Code ${resp.code()}")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("No connection to server")
            }
        }
        return  stringResp
    }

    fun getSpecializationNameList(): LiveData<List<String>> {
//        getSpecializationInfoFromServ()
        return specDAO.getSpecsName()
    }


    fun getSpecializationIdByName(name: String): LiveData<Int> {
        return specDAO.getSpecializationId(name)
    }
    fun getSpecializationNameListByTypeName(skillTypeName: String): LiveData<List<String>> {
        return specDAO.getSpecsNameByTypeName(skillTypeName)
    }
    fun getEducationsBySpecName(name: String): LiveData<List<String>> {
        return eduDAO.getEducationsBySpecName(name)
    }
    fun getEducationId(name: String): LiveData<Int> {
        return eduDAO.getEducationId(name)
    }
    fun getSpecializationBySkillTypeIdFromServ(skillTypeId: Int) {
        launch(Dispatchers.IO) {
            try{
                val resp = specAPI.getSpecializationBySkillTypeAsync(skillTypeId)
                if (resp.isSuccessful){
                    val spec = resp.body()
                    spec?.let {special->
                        specDAO.addItems(*special.toTypedArray())
                        var result = special.map { SpecializationTypeCrossRef(skillTypeId, it.specId)}

                        specDAO.addSpecsTyped(*result.toTypedArray())
                    }

                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    fun getSpecializationModedFromServ(mod: String) {
        launch(Dispatchers.IO) {
            try{
                val resp = specAPI.getSpecsModAsync(mod)
                if (resp.isSuccessful){
                    val spec = resp.body()
                    spec?.let {special->
                        specDAO.addItems(*special.toTypedArray())
                    }

                }
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    fun getSpecializationInfoFromServ(){
        launch(Dispatchers.IO){
            try{
                val resp = specToEduAPI.getSpecToEduAsync()
                if (resp.isSuccessful){
                    specDAO.deleteSpecToEduData()
                    val jsonObject = resp.body()
                    if (jsonObject != null){
                        for (i in jsonObject){
                            val specId = i.get("id").asInt
                            val spec = Specialization(
                                specId,
                                i.get("name").asString,
                                null
                            )
                            specDAO.addItem(spec)
                            val educationJson = i.getAsJsonArray("education")
                            for (j in 0..< educationJson.size()) {
                                val item = educationJson.get(j).asJsonObject
                                item.let {
                                    val eduId = it.get("id").asInt
                                    val education = Education(
                                        eduId,
                                        it.get("name").asString,
                                        null
                                    )
                                    eduDAO.addItem(education)
                                    specDAO.addSpecToEdu(SpecializationToEducationCrossRef(eduId,specId))
                                }
                            }
                        }
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}