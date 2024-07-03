package ru.kondrashen.diplomappv20.repository.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.ArchiveDAO
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddArchive
import ru.kondrashen.diplomappv20.repository.responces.PostResponse


class ArchiveRepository(private var archiveDAO: ArchiveDAO): CoroutineScope {


    private val archiveAPI = APIFactory.archiveApi

    private lateinit var stringResp: MutableLiveData<String>
    private lateinit var customResp: MutableLiveData<PostResponse>
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job

    fun getUserArchives(userId: Int): LiveData<List<Archive>>{
        return archiveDAO.getUserArchives(userId)
    }
    fun getCurArchiveNameById(archId: Int): LiveData<Archive>{
        return archiveDAO.getCurArchiveNameById(archId)
    }

    fun postArchiveToServ(token: String?, name: String, userId: Int):LiveData<PostResponse>{
        val resultToken = "Bearer $token"
        customResp = MutableLiveData<PostResponse>()
        launch {
            try{
                val resp = archiveAPI.postArchiveAsync(resultToken, userId, AddArchive(name))
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    val result = respBody?.get("status")?.asString
                    val archId = respBody?.get("archId")?.asInt
                    customResp.postValue(PostResponse(result?: "No connection to server", archId))
                    if(archId != null && result == "succes")
                        postArchiveToRoom(name, userId, archId)

                }
                else
                    customResp.postValue(PostResponse("Server Response Code ${resp.code()}", null))
            } catch (e: Exception) {
                e.printStackTrace()
                customResp.postValue(PostResponse("No connection to server", null))
            }
        }
        return customResp
    }

    fun putArchiveToServ(token: String?, name: String, userId: Int, archiveId: Int):LiveData<String>{
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch {
            try{
                val resp = archiveAPI.putArchiveAsync(resultToken, userId, archiveId, AddArchive(name))
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    val result = respBody?.get("status")?.asString
                    stringResp.postValue(result?: "No response")
                    putArchiveToRoom(name, archiveId)
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
    fun postArchiveToRoom(name: String, userId: Int, archiveId: Int){
        launch (Dispatchers.IO){
            archiveDAO.addItem(Archive(archiveId, name, name, userId))
        }
    }
    fun putArchiveToRoom(name: String, archiveId: Int){
        launch (Dispatchers.IO){
            archiveDAO.updateArchiveById(archiveId, name)
        }
    }

    fun deleteArchiveByIdFromServ( token: String?, archiveId: Int, userId: Int): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch (Dispatchers.IO){
            try{
                val resp = archiveAPI.deleteArchiveByIdAsync(resultToken, userId, archiveId)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    var result = respBody?.get("status")?.asString
                    stringResp.postValue(result?: "No connection to server")
                    deleteArchiveByIdFromRoom(archiveId)
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
    private fun deleteArchiveByIdFromRoom(archId: Int){
        archiveDAO.deleteArchiveById(archId)
    }
//    fun deleteDocRespFromRoom(docRespId: Int){
//        archiveDAO.deleteArchiveRespByArchId(docRespId)
//    }
}