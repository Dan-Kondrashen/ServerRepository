package ru.kondrashen.diplomappv20.repository.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.ExperienceDAO
import ru.kondrashen.diplomappv20.repository.dao.ExperienceTimeDAO
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo
import kotlin.coroutines.CoroutineContext

class ExperienceRepository(private val expDAO: ExperienceDAO, private val expTimeDAO: ExperienceTimeDAO): CoroutineScope {
    private lateinit var stringResp: MutableLiveData<String>
    private var stringRespExp: MutableLiveData<String> = MutableLiveData<String>()
    private var TAG = "ExperienceRep"
    private val userAPI = APIFactory.userApi
    private val docAPI = APIFactory.docApi
    private val expAPI = APIFactory.expApi
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job
    fun getExperienceFullInfoByExpId(expId: Int): LiveData<ExperienceInfo> {
        return expDAO.getExperienceFullInfoByExpId(expId)
    }
    fun getAppExpInfoByExpId(expId: Int): LiveData<UserExperience> {
        return expDAO.getAppExpInfoById(expId)
    }
    fun getExperienceFullInfoByUserId(userId: Int): LiveData<List<ExperienceInfo>> {
        return expDAO.getExperienceFullInfoByUserId(userId)
    }
    fun getExperienceFullInfoByDocId(docId: Int): LiveData<List<ExperienceInfo>> {
        return expDAO.getExperienceFullInfoByDocId(docId)
    }
    fun getExperienceTimesByDocId(docId: Int): LiveData<List<String>> {
        return expDAO.getExperienceTimeByDocId(docId)
    }
//    fun getExperienceLiveInfo(): LiveData<String>{
//        return stringRespExp
//    }
    fun getExperienceInfoByUserIdFromServer(curUserId: Int, userId: Int): LiveData<String> {
        stringRespExp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try {
                var resp = expAPI.getUserExperienceAsync(curUserId, userId)
                if (resp.isSuccessful) {
                    val expList = resp.body()
                    expList?.let {
                        expDAO.addItems(*expList.toTypedArray())
                    }
                    stringRespExp.postValue("success")
                }
                else{
                    stringRespExp.postValue("${resp.code()}")
                }

            }
            catch (e: Exception){
                e.printStackTrace()
                stringRespExp.postValue("timeout")
            }
        }
        return stringRespExp
    }


    fun getAppExpInfoByUserIdFromRoom(userId: Int): LiveData<List<UserExperience>> {
        return expDAO.getAppExpList(userId)
    }

    fun getAppExpInfoByUserIdFromServer(token: String?, curUserId: Int, userId: Int): LiveData<String> {
        stringRespExp = MutableLiveData<String>()
        val resultToken = "Bearer $token"
        launch(Dispatchers.IO) {
            try {
                var resp = expAPI.getUserAppExpAsync(resultToken, curUserId, userId)
                if (resp.isSuccessful) {
                    val expList = resp.body()
                    expList?.let {
                        expDAO.addAppExpItems(*expList.toTypedArray())
                    }
                    stringRespExp.postValue("success")
                }
                else{
                    stringRespExp.postValue("${resp.code()}")
                }

            }
            catch (e: Exception){
                e.printStackTrace()
                stringRespExp.postValue("timeout")
            }
        }
        return stringRespExp
    }

    fun postAppExpInfoByUserIdToServer(token: String?, curUserId: Int, userId: Int,
                                       addFile: MultipartBody.Part,
                                       name: RequestBody, type: RequestBody,
                                       reason: RequestBody, points: RequestBody,
                                       status: RequestBody): LiveData<String> {
        stringRespExp = MutableLiveData<String>()
        val resultToken = "Bearer $token"
        launch(Dispatchers.IO) {
            try {
                var resp = expAPI.postUserAppExpAsync(resultToken, curUserId, userId,
                    name, type, reason, status, points, addFile)
                if (resp.isSuccessful) {
                    val expJson= resp.body()
                    expJson?.let {
                        val expInfo = expJson.getAsJsonObject("userExp")
                        expInfo?.let {
                            val expItem = UserExperience(
                                id = expInfo.get("id").asInt,
                                reason = if (expInfo.get("reason").isJsonNull) null
                                else expInfo.get("reason").asString,
                                points = expInfo.get("points").asInt,
                                type = expInfo.get("type").asString,
                                status = expInfo.get("status").asString,
                                userId = expInfo.get("userId").asInt,
                                documents_scan_id = if (expInfo.get("documents_scan_id").isJsonNull)
                                    null
                                else expInfo.get("documents_scan_id").asInt,
                            )
                            expDAO.addAppExpItem(expItem)
                        }

                    }
                    stringRespExp.postValue("success")
                }
                else{
                    stringRespExp.postValue("${resp.code()}")
                }

            }
            catch (e: Exception){
                e.printStackTrace()
                stringRespExp.postValue("timeout")
            }
        }
        return stringRespExp
    }
    fun putAppExpInfoByUserIdToServer(token: String?, curUserId: Int, userId: Int, expId: Int,
                                       addFile: MultipartBody.Part,
                                       name: RequestBody, type: RequestBody,
                                       reason: RequestBody, points: RequestBody,
                                       status: RequestBody): LiveData<String> {
        stringRespExp = MutableLiveData<String>()
        val resultToken = "Bearer $token"
        launch(Dispatchers.IO) {
            try {
                var resp = expAPI.putUserAppExpAsync(resultToken, curUserId, userId, expId,
                    name, type, reason, status, points, addFile)
                if (resp.isSuccessful) {
                    val expJson= resp.body()
                    expJson?.let {
                        val expInfo = expJson.getAsJsonObject("userExp")
                        expInfo?.let {
                            val expItem = UserExperience(
                                id = expInfo.get("id").asInt,
                                reason = if (expInfo.get("reason").isJsonNull) null
                                else expInfo.get("reason").asString,
                                points = expInfo.get("points").asInt,
                                type = expInfo.get("type").asString,
                                status = expInfo.get("status").asString,
                                userId = expInfo.get("userId").asInt,
                                documents_scan_id = if (expInfo.get("documents_scan_id").isJsonNull)
                                    null
                                else expInfo.get("documents_scan_id").asInt,
                            )
                            expDAO.addAppExpItem(expItem)
                        }

                    }
                    stringRespExp.postValue("success")
                }
                else{
                    stringRespExp.postValue("${resp.code()}")
                }
            }
            catch (e: Exception){
                e.printStackTrace()
                stringRespExp.postValue("timeout")
            }
        }
        return stringRespExp
    }

    fun getExperienceFullInfoFilterIdListByUserId(userId: Int, iDsList: List<Int>, mod: String): LiveData<List<ExperienceInfo>> {
        return when(mod){
            "in" -> expDAO.getExperienceInfoWithIdsByUserId(userId, iDsList)
            else -> expDAO.getExperienceInfoWithoutIdsByUserId(userId, iDsList)
        }
    }

    fun getExperienceTimeNameList(): LiveData<List<String>> {
        return expTimeDAO.getExperienceTimeNames()
    }

    fun getExperienceTimeId(name: String): LiveData<Int> {
        return expTimeDAO.getExperienceTimeId(name)
    }

    fun postExpFileToServ(token: String?,authUserId: Int,  userId: Int, addFile: MultipartBody.Part,
                          name: RequestBody, role: RequestBody,
                          place: RequestBody, expTimeId: RequestBody,
                          experience: RequestBody): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        try {
            launch(Dispatchers.IO) {
                val resp = expAPI.postExpFileAuth(
                    resultToken,
                    authUserId,
                    userId,
                    name,
                    role,
                    place,
                    experience,
                    expTimeId,
                    addFile
                )
                if (resp.isSuccessful) {
                    println("Получил ответ")
                    val jsonObject = resp.body()
                    if (jsonObject != null) {
                        if (jsonObject.isJsonNull)
                            stringResp.postValue("Server Response Code ${resp.code()}")
                        else{
                            jsonObject?.get("status")?.let {
                                val result = it.asString
                                stringResp.postValue(result)
                            }
                        }
                    }
                } else
                    stringResp.postValue("Server Response Code ${resp.code()}")
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            stringResp.postValue("no connection")
        }
        return  stringResp
    }
    fun putExpFileToServ(token: String?, userId: Int, expId: Int, addFile: MultipartBody.Part,
                          name: RequestBody, role: RequestBody,
                          place: RequestBody, expTimeId: RequestBody,
                          experience: RequestBody): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            val resp= expAPI.putExpFileAuth(resultToken, userId,expId, name, role, place, experience , expTimeId, addFile)
            if (resp.isSuccessful) {
                val jsonObject = resp.body()
                jsonObject?.get("status")?.let {
                    val result = it.asString
                    stringResp.postValue(result)
                }
            }
            else
                stringResp.postValue("Server Response Code ${resp.code()}")
        }
        return  stringResp
    }
    fun deleteAppExpUserId(token: String?, authUserId: Int, userId: Int, expId: Int): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch {
            try {
                var resp = expAPI.deleteUserAppExpAsync(resultToken, authUserId, userId, expId)
                if (resp.isSuccessful) {
                    var result = resp.body()
                    result?.let {
                        val res = it.get("status").asString
                        stringResp.postValue(res)
                        if(res != "not allowed"){
                            expDAO.deleteAppExp(expId)
                        }
                    }
                }
                else {

                    stringResp.postValue("${resp.code()}")
                    expDAO.deleteAppExp(expId)
                    Log.i(TAG,"bad request from serv")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("no connection")
                Log.i(TAG,"no connection exception")
            }
        }
        return stringResp
    }
    fun deleteExperienceUserId(token: String?, userId: Int, expId: Int): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch {
            try {
                var resp = expAPI.deleteExpFileAuth(resultToken, userId, expId)
                if (resp.isSuccessful) {
                    var result = resp.body()
                    result?.let {
                        stringResp.postValue(it.status)
                        if(it.status != "not allowed"){
                            expDAO.deleteExperience(expId)
                        }
                    }
                }
                else {
                    stringResp.postValue("${resp.code()}")
                    expDAO.deleteExperience(expId)
                    Log.i(TAG,"bed request from serv")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("no connection")
                Log.i(TAG,"no connection exception")
            }
        }
        return stringResp
    }
}