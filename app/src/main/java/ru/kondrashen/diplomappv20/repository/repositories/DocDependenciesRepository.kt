package ru.kondrashen.diplomappv20.repository.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.DependenciesDAO
import ru.kondrashen.diplomappv20.repository.dao.DocumentDAO
import ru.kondrashen.diplomappv20.repository.dao.ViewsDAO
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Views
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddArchive
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentAnalysisInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.responces.PostResponse
import kotlin.coroutines.CoroutineContext

class DocDependenciesRepository(private val docDAO: DocumentDAO, private val docDependDAO: DependenciesDAO, private val docViewsDAO: ViewsDAO): CoroutineScope {

    private lateinit var stringResp: MutableLiveData<String>
    private var stringRespSpec: MutableLiveData<String> = MutableLiveData<String>()
    private var TAG = "DocDependenciesRep"
    private val docAPI = APIFactory.docApi
    private val dependAPI = APIFactory.dependApi
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job

    fun getDocumentById(id: Int): LiveData<DocumentInfoWithKnowledge> {
        return docDAO.getDocumentById(id)
    }
    fun getDependenceInfoByDocumentId(id: Int): LiveData<List<DocDependenceFullInfo>> {
        return docDependDAO.getDependenciesInfoByDocId(id)
    }
    fun getDependenceNameByDocId(id: Int): LiveData<List<String>> {
        return docDependDAO.getDependenciesNameByDocId(id)
    }
    fun getDependenceById(dependId: Int): LiveData<DocDependenceFullInfo> {
        return docDependDAO.getDependenceById(dependId)
    }
    fun getDependenceInfoByUserId(id: Int): LiveData<List<DocDependenceFullInfo>> {
        return docDependDAO.getDependenciesInfoByUserId(id)
    }
//    fun getDependenceLiveInfo(): LiveData<String>{
//        return stringRespSpec
//    }
    fun getDocDependenceUserId(userId: Int): LiveData<String> {
        stringRespSpec = MutableLiveData<String>()
        launch {
            try {
                var resp = dependAPI.getUserDependenciesAsync(userId)
                if (resp.isSuccessful){
                    var result = resp.body()
                    stringRespSpec.postValue("success")
                    result?.let {
                        docDependDAO.addItems(*it.toTypedArray())
                    }
                }
                else{
                    stringRespSpec.postValue("${resp.code()}")
                }
            }
            catch (e: Exception){
                e.printStackTrace()
                stringRespSpec.postValue("timeout")
            }
        }
        return stringRespSpec
    }
    fun getFileMimeUserId(token: String?, userId: Int, fileId: Int): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            var resp = dependAPI.getFileMimeAsync(resultToken, fileId, userId)
            if (resp.isSuccessful) {
                val type = resp.headers()["Content-Type"]
                type?.let {
                    stringResp.postValue(it)
                }
            }
            else
                stringResp.postValue("bed request")
        }
        return stringResp
    }
    fun deleteDocDependenceUserId(token: String?, userId: Int, dependId: Int): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try {
                var resp = dependAPI.deleteDependenciesFileAuth(resultToken, userId, dependId)
                if (resp.isSuccessful) {
                    var result = resp.body()
                    result?.let {
                        stringResp.postValue(it.status)
                        if(it.status != "not allowed"){
                            docDependDAO.deleteDepend(dependId)
                        }
                    }
                }
                else {
                    stringResp.postValue("bed request")
                    docDependDAO.deleteDepend(dependId)
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
    fun getDependenceInfoFilterIdListByUserId(userId: Int, iDsList: List<Int>, mod: String): LiveData<List<DocDependenceFullInfo>> {
        return when(mod){
            "in" -> docDependDAO.getDependenciesInfoWithIdByUserId(userId, iDsList)
            else -> docDependDAO.getDependenciesInfoWithoutIdByUserId(userId, iDsList)
        }
    }
    fun getDocsAnalysisInfoByUserId(userId: Int): LiveData<List<DocumentAnalysisInfo>>{
        return  docDAO.getUserDocAnalysisByUserId(userId)
    }
    fun getUserDocsFromServ(token: String?, userId: Int, mod: String): LiveData<String>{
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try {
                val resp = docAPI.getUserDocumentInfoAsync(resultToken, userId, mod)
                if (resp.isSuccessful) {
                    val jsonObject = resp.body()
                    println(jsonObject)
                    jsonObject?.let {
                        val documentJson = it.getAsJsonArray("documents")
                        val viewsDocJson = it.getAsJsonArray("views")
                        if (documentJson.size() != 0){
                            when(mod) {
                                "all" -> {
                                    for (j in 0..<documentJson.size()) {
                                        val item = documentJson.get(j).asJsonObject
                                        item.let {
                                            val doc = Document(
                                                item.get("id").asInt,
                                                item.get("title").asString,
                                                if (item.get("salaryF").isJsonNull) 0F
                                                else item.get("salaryF").asFloat,
                                                if (item.get("salaryS").isJsonNull) 0F
                                                else item.get("salaryS").asFloat,
                                                item.get("extra_info").toString(),
                                                item.get("contactinfo").toString(),
                                                item.get("type").asString,
                                                item.get("userId").asInt,
                                                item.get("date").toString(),
                                            )
                                            println(doc)
                                            docDAO.addDocument(doc)
                                        }
                                    }
                                }
                                "analysis" ->{
                                    for (j in 0..<documentJson.size()) {
                                        val item = documentJson.get(j).asJsonObject
                                        item.let {
                                            val doc = Document(
                                                docId = item.get("id").asInt,
                                                title = item.get("title").asString,
                                                type = item.get("type").asString,
                                                userId = item.get("userId").asInt,
                                                contactinfo = "-", extra_info = "-",
                                                date = item.get("date").toString(),
                                                salaryS = null, salaryF = null)
                                            docDAO.addDocument(doc)
                                        }
                                    }
                                }
                            }
                        }
                        if (viewsDocJson.size() != 0){
                            for (j in 0..< viewsDocJson.size()) {
                                val item = viewsDocJson.get(j).asJsonObject
                                item.let {
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
                    stringResp.postValue("success")
                } else
                    stringResp.postValue("no connection")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("timeout")
            }
        }
        return  stringResp
    }

}