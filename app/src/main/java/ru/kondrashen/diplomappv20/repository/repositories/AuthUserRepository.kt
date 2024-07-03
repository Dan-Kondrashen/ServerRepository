package ru.kondrashen.diplomappv20.repository.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.ArchiveDAO
import ru.kondrashen.diplomappv20.repository.dao.DocumentDAO
import ru.kondrashen.diplomappv20.repository.dao.LevelDAO
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddUser
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLog
import ru.kondrashen.diplomappv20.repository.dao.UserDAO
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.Level
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.UserLevel
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddArchive
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocumentFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.CallUserListPref
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.FullCurrentUserInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UpdateUser
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLevelInfoForProgressView
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserTokenAndId
import ru.kondrashen.diplomappv20.repository.responces.AuthResponse


class AuthUserRepository(private val userDAO: UserDAO, private val docDAO: DocumentDAO, private var archiveDAO: ArchiveDAO, private var levelDAO: LevelDAO): CoroutineScope {

    private val userAPI = APIFactory.userApi
    private val docAPI = APIFactory.docApi
    private val archiveAPI = APIFactory.archiveApi
//    private var users: LiveData<List<User>> = userDAO.getUsers()
    private lateinit var resp: AuthResponse
    private lateinit var stringResp: MutableLiveData<String>
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job
    companion object{
        const val TAG = "UserRepository"
    }
    private fun getDataFromServer() {
        launch(Dispatchers.IO) {
            val resp = userAPI.getUsersAsync() as MutableList<User>
            for (i in resp)
                userDAO.addItem(i)
        }
    }
    fun saveFBToken( userId: Int, token: String, updatedUserId: Int){
        launch {
            try {
                userAPI.postUserTokenAsync(userId, UserTokenAndId(token, updatedUserId))
            }
            catch (e: Exception){
                e.printStackTrace()
                Log.w(TAG, "Ошибка при отправке данных на сервер")
            }
        }
    }
    fun getUser(userId: Int): LiveData<User>{
        return userDAO.getUser(userId)
    }

    fun getUserLevelInfo(userId: Int): LiveData<UserLevelInfoForProgressView>{
        return levelDAO.getUserLevelInfo(userId)
    }

    fun getUserArchives(userId: Int): LiveData<List<Archive>>{
        return archiveDAO.getUserArchives(userId)
    }

    fun getUserArchivesStatic(userId: Int): List<Archive>{
        return archiveDAO.getUserArchivesStatic(userId)
    }

    fun getUsersListServ(token: String?, userId: Int, info: CallUserListPref): LiveData<String>{
        stringResp = MutableLiveData<String>()
        val resultToken = "Bearer $token"
        launch(Dispatchers.IO) {
            try {
                val resp = userAPI.getUsersListAdminAsync(resultToken, userId, info)
                if (resp.isSuccessful) {
                    var users = resp.body()
                    users?.let {
                        userDAO.addItems(*users.toTypedArray())
                    }
                }
                else
                    stringResp.postValue("bed request")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("No connection")
            }
        }
        return stringResp
    }
    fun getUserListRoom(userId: Int, userType: Int, info: CallUserListPref):LiveData<List<User>>{
        if (info.roleIDs == null && info.status == null)
            return userDAO.getUsersAdminSimple(userId, userType)
        else
            return userDAO.getUsersAdmin(userId, userType)
    }

    fun getUserFromServ(userId: Int): LiveData<String>{
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try {
                println(userId.toString() +"вот")
                val resp = userAPI.getUserAsync(userId)
                if (resp.isSuccessful) {
                    val jsonObject = resp.body()
                    jsonObject?.let {
                        val user = User(
                            userId,
                            it.get("fname").asString,
                            it.get("lname").asString,
                            if (it.get("mname").isJsonNull) null else it.get("mname").asString,
                            if (it.get("status").isJsonNull) null else it.get("status").asString,
                            it.get("phone").asLong,
                            it.get("email").asString,
                            it.get("roleId").asInt,
                            it.get("registration_date").toString(),
                        )
                        userDAO.addItem(user)

                        println(it.get("levelNum").asString+ "вот")
                        userDAO
                        val level = Level(
                            number = it.get("levelNum").asInt,
                            maxPoints =  it.get("levelMaxP").asInt,
                            minPoints =  it.get("levelMinP").asInt,
                            id = it.get("levelId").asInt,
                        )
                        println(level.toString() +"djn")
                        val curLevel = UserLevel(
                            userId = userId,
                            curPoints = it.get("curPoints").asInt,
                            levelId = it.get("levelId").asInt,
                            id = it.get("userCurLevelInfoId").asInt,
                        )
                        levelDAO.addItem(level)
                        levelDAO.addUserLevel(curLevel)
                        when (it.get("respStatus").asString){
                            "full data level" -> {
                                println("full")
                                val levelPreviews = Level(
                                    number = it.get("levelNum").asInt -1,
                                    maxPoints =  it.get("levelMinP").asInt,
                                    minPoints =  it.get("prevLevelMinP").asInt,
                                    id = it.get("prevLevelId").asInt
                                )
                                levelDAO.addItem(levelPreviews)
                                val levelNext= Level(
                                    number = it.get("levelNum").asInt + 1,
                                    minPoints =  it.get("levelMaxP").asInt,
                                    maxPoints =  it.get("nextLevelMaxP").asInt,
                                    id = it.get("nextLevelId").asInt
                                )
                                levelDAO.addItem(levelNext)
                            }
                            "no previews level"->{
                                val levelNext= Level(
                                    number = it.get("levelNum").asInt + 1,
                                    minPoints =  it.get("levelMaxP").asInt,
                                    maxPoints =  it.get("nextLevelMaxP").asInt,
                                    id = it.get("nextLevelId").asInt
                                )
                                levelDAO.addItem(levelNext)
                            }
                            "no next level" ->{
                                val levelPreviews = Level(
                                    number = it.get("levelNum").asInt -1,
                                    maxPoints =  it.get("levelMinP").asInt,
                                    minPoints =  it.get("prevLevelMinP").asInt,
                                    id = it.get("prevLevelId").asInt
                                )
                                levelDAO.addItem(levelPreviews)
                            }
                        }
                        println("$user user")
                        userDAO.addItem(user)
                        val archiveJsonArray = it.getAsJsonArray("archive")
                        if (archiveJsonArray.size() != 0) {
                            for (j in 0..<archiveJsonArray.size()) {
                                val item = archiveJsonArray.get(j).asJsonObject
                                item.let {
                                    val archive = Archive(
                                        item.get("id").asInt,
                                        item.get("name").asString,
                                        item.get("searchableWord").asString,
                                        userId
                                    )
                                    println(archive.toString()+"votvot")
                                    userDAO.addUserArchive(archive)
                                }
                            }
                        }
                    }
                    stringResp.postValue("OK")
                } else
                    stringResp.postValue("No connection")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return  stringResp
    }
    fun getCurUser(userId: Int): LiveData<FullCurrentUserInfo>{
        return userDAO.getCurUserAccauntInfo(userId)
    }
    fun postLogData(log: UserLog): AuthResponse {
        try {
            runBlocking {
                resp = userAPI.postLoginData(log)
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            resp = AuthResponse("Сервер не отвечает!", 0, "", "")
        }
        return resp
    }
    fun postRegData(regUser: AddUser): AuthResponse{
        runBlocking {
            try {
                resp= userAPI.postUserAsync(regUser)
            } catch (e: Exception) {
                e.printStackTrace()
                resp = AuthResponse("Сервер не отвечает!", 0, "", "")
            }
        }
        return resp
    }
    fun putUserToServ(token: String?, curUserId: Int, userId: Int, user: UpdateUser): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        try {
            launch {
                var putResp = userAPI.putUserAsync(resultToken, curUserId, userId, user)
                if (putResp.isSuccessful){
                    stringResp.postValue("success")
                    var result = putResp.body()
                    result?.let {
                        stringResp.postValue(it.get("status").asString)
                        val userInfo = it.getAsJsonObject("user")
                        userInfo?.let { userInf->
                            var id = userInfo.get("id").asInt
                            var fname = userInfo.get("fname").asString
                            var lname= userInfo.get("lname").asString
                            var mname =  if (userInfo.get("mname").isJsonNull) null else userInfo.get("mname").asString
                            var email = userInfo.get("email").asString
                            var phone = userInfo.get("phone").asLong
                            println("Result" + fname + email + mname.toString() + phone)
                            userDAO.putUser(id, fname, lname,mname, email, phone)
                        }
                    }
                }
                else{
                    stringResp.postValue("bad response")
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            stringResp.postValue("timeout")
        }
        return stringResp
//        return userRep.putUserToServ(token, curUserId, userId, user)

    }

    fun deleteDocRespByIdFromServ(respId: Int, userId: Int, token: String?): LiveData<String>{
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch (Dispatchers.IO){
            try{
                val resp = userAPI.deleteUserResponseById(resultToken, userId, respId)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    var result = respBody?.get("status")?.asString
                    if (result == "Not found") {
                        stringResp.postValue("Данный документ не найден на сервере, поэтому он будет удален только локально!")
                        deleteDocRespFromRoom(respId)
                    }
                    else {
                        stringResp.postValue("Успешно удалено!")
                        deleteDocRespFromRoom(respId)
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
    fun postDocumentToServ(token: String?, document: AddDocumentFullInfo, userId: Int, mod: String):LiveData<String>{
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch {
            try{
                val resp = docAPI.postNewUserDocumentAsync(resultToken, userId, mod, document)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    val result = respBody?.get("status")?.asString
                    if (result == "Not at all") {
                        stringResp.postValue("not at all!")
                    }
                    else{
                        stringResp.postValue("success")
                    }
                }
                else
                    stringResp.postValue("${resp.code()}")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("timeout")
            }
        }
        return stringResp
    }

    fun postArchiveToServ(token: String?, name: String, userId: Int):LiveData<String>{
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch {
            try{
                val resp = archiveAPI.postArchiveAsync(resultToken, userId, AddArchive(name))
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    val result = respBody?.get("status")?.asString
                    stringResp.postValue(result?: "No connection to server")
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

    fun deleteDocByIdFromServ(docId: Int, userId: Int, token: String?): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch (Dispatchers.IO){
            try{
                val resp = userAPI.deleteUserDocumentById(resultToken, userId, docId)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    var result = respBody?.get("status")?.asString
                    if (result == "Not found")
                        stringResp.postValue("Данный документ не найден на сервере!")
                    else {
                        stringResp.postValue("Успешно удалено!")
                        deleteDocByIdFromRoom(docId, userId)
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
    private fun deleteDocByIdFromRoom(docId: Int, userId: Int){
        docDAO.deleteDocumentByIdFromRoom(docId, userId)
    }
    fun deleteDocRespFromRoom(docRespId: Int){
        docDAO.deleteRespById(docRespId)
    }
}