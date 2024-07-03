package ru.kondrashen.diplomappv20.repository.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.ResponseDAO
import ru.kondrashen.diplomappv20.repository.dao.ViewsDAO
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Views
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocResponse
import kotlin.coroutines.CoroutineContext

class DocResponsesRepository(private val docResponseDAO: ResponseDAO, private val  docViewsDAO: ViewsDAO):CoroutineScope {
    private val respAPI = APIFactory.responseApi
    private lateinit var stringResp: MutableLiveData<String>
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job

    fun getDocResponseByUserId(userId: Int, docId: Int): LiveData<List<String>>{
        return docResponseDAO.getResponsesNameByDocAndUser(userId, docId)
    }
    fun getDocResponseByUserIdWithoutNames(userId: Int, docId: Int, types: List<String>): LiveData<List<String>>{
        return docResponseDAO.getResponsesIdByDocAndUserWithoutNames(userId, docId, types)
    }

    fun postResponseToServ(token: String?, docRespNames: List<String>, userId: Int, docId: Int):LiveData<String>{
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try{
                var docRespList = mutableListOf<AddDocResponse>()
                for (i in docRespNames){
                    docResponseDAO.addItem(DocResponse(null,i, userId, docId, "new"))
                    docRespList.add(AddDocResponse(i, userId, docId, "new"))
                }
                val gson = Gson()
                val jsonMas = JsonObject()
                jsonMas.add("responses",gson.toJsonTree(docRespList))
                val resp = respAPI.postDocResponseListAsync(resultToken, userId, jsonMas)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    stringResp.postValue("success")
                    respBody?.let {
                        val viewsDocJson = respBody.getAsJsonArray("views")
                        val responseDocJson = respBody.getAsJsonArray("responses")
                        if (responseDocJson.size() != 0) {
                            for (j in 0..<responseDocJson.size()) {
                                val item = responseDocJson[j].asJsonObject
                                val response = DocResponse(
                                    docId = item.get("docId").asInt,
                                    status = item.get("statys").asString,
                                    id = item.get("id").asInt,
                                    userId = item.get("userId").asInt,
                                    type = item.get("type").asString,
                                )
                                docResponseDAO.updateRespByParam(response.id!!, response.userId, response.type, response.docId)
                            }
                        }
                        if (viewsDocJson.size() != 0){
                            for (j in 0..<viewsDocJson.size()) {
                                val item = viewsDocJson[j].asJsonObject
                                val view = Views(
                                    docId = item.get("docId").asInt,
                                    numviews = item.get("numUsages").asInt,
                                    typeS = item.get("type").asString,
                                    id = null
                                )
                                docViewsDAO.deleteCopy(view.docId, view.typeS)
                                docViewsDAO.addItem(view)
                            }
                        }

                    }
                }
                else
                    stringResp.postValue("Server Response Code ${resp.code()}")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("No connection to server")
            }
        }
        return stringResp
    }
    fun postResponseToServ2(token: String?, docRespNames: List<String>, userId: Int, docId: Int){
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try{
                var docRespList = mutableListOf<AddDocResponse>()
                for (i in docRespNames){
                    docResponseDAO.addItem(DocResponse(null,i, userId, docId, "new"))
                    docRespList.add(AddDocResponse(i, userId, docId, "new"))
                }
                val gson = Gson()
                val jsonMas = JsonObject()
                jsonMas.add("responses",gson.toJsonTree(docRespList))
                val resp = respAPI.postDocResponseListAsync(resultToken, userId, jsonMas)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    respBody?.let {
                        val viewsDocJson = respBody.getAsJsonArray("views")
                        val responseDocJson = respBody.getAsJsonArray("responses")
                        if (responseDocJson.size() != 0) {
                            for (j in 0..<responseDocJson.size()) {
                                val item = responseDocJson[j].asJsonObject
                                val response = DocResponse(
                                    docId = item.get("docId").asInt,
                                    status = item.get("statys").asString,
                                    id = item.get("id").asInt,
                                    userId = item.get("userId").asInt,
                                    type = item.get("type").asString,
                                )
                                docResponseDAO.updateRespByParam(response.id!!, response.userId, response.type, response.docId)
                            }
                        }
                        if (viewsDocJson.size() != 0){
                            for (j in 0..<viewsDocJson.size()) {
                                val item = viewsDocJson[j].asJsonObject
                                val view = Views(
                                    docId = item.get("docId").asInt,
                                    numviews = item.get("numUsages").asInt,
                                    typeS = item.get("type").asString,
                                    id = null
                                )
                                docViewsDAO.deleteCopy(view.docId, view.typeS)
                                docViewsDAO.addItem(view)
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