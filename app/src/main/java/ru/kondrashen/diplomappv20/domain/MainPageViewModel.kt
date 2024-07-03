package ru.kondrashen.diplomappv20.domain

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ru.kondrashen.diplomappv20.repository.responces.PostResponse
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.CallUserListPref
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ChatTitleAndAnotherUserNick
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.CommWithResp
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentPreference
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ResponseInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLevelInfoForProgressView
import ru.kondrashen.diplomappv20.repository.db.WorkSearcherDB
import ru.kondrashen.diplomappv20.repository.repositories.AuthUserRepository
import ru.kondrashen.diplomappv20.repository.repositories.CommentsRepository
import ru.kondrashen.diplomappv20.repository.repositories.DocumentsRepository

class MainPageViewModel(application: Application): AndroidViewModel(application) {
    private val docRep: DocumentsRepository
    private val userRep: AuthUserRepository
    private val commRep: CommentsRepository

    val pref = application.getSharedPreferences("AuthPref", Context.MODE_PRIVATE)
    val editor = pref.edit()
    private val token = pref.getString("token", null)


    init {
        val userDao = WorkSearcherDB.getDatabase(application).authDao()
        val docDao = WorkSearcherDB.getDatabase(application).docDao()
        val docDepDao = WorkSearcherDB.getDatabase(application).docDependDao()
        val docRespDao = WorkSearcherDB.getDatabase(application).docResponseDao()
        val docViewDao = WorkSearcherDB.getDatabase(application).docViewDao()
        val specDao = WorkSearcherDB.getDatabase(application).specDao()
        val eduDao = WorkSearcherDB.getDatabase(application).eduDao()
        val expDao = WorkSearcherDB.getDatabase(application).expDao()
        val expTimeDao = WorkSearcherDB.getDatabase(application).expTimeDao()
        val knowDao = WorkSearcherDB.getDatabase(application).knowDao()
        val archiveDao = WorkSearcherDB.getDatabase(application).archiveDao()
        val commentDao = WorkSearcherDB.getDatabase(application).commentDao()
        val levelDao = WorkSearcherDB.getDatabase(application).levelDao()
        docRep = DocumentsRepository(docDao, userDao, specDao, eduDao, knowDao, docDepDao, docRespDao, docViewDao, expDao, expTimeDao, commentDao)
        userRep =  AuthUserRepository(userDao, docDao, archiveDao, levelDao)
        commRep =  CommentsRepository(commentDao, userDao)
    }
    //Работа с данными пользователя
    fun saveFBTokenToServ(userId: Int, token: String){
        commRep.saveFBToken(userId, token, userId)
    }

    fun getUserLevelData(userId: Int): LiveData<UserLevelInfoForProgressView> {
        return userRep.getUserLevelInfo(userId)
    }

    fun getCurrentUserDataFromServ(userId: Int): LiveData<String> {
        return userRep.getUserFromServ(userId)
    }

    fun getMainUsersListDataServ(userId: Int, info: CallUserListPref): LiveData<String>{
        return userRep.getUsersListServ(token, userId, info)
    }
    fun getMainUsersListDataRoom(userId: Int, userType: Int, info: CallUserListPref): LiveData<List<User>>{
        return userRep.getUserListRoom(userId, userType, info)
    }


    fun getMainPageDataFromRoom(type: String, mod: String): LiveData<List<DocumentInfo>>{
        return docRep.getDocsInfoByType(type, mod)
    }
    fun getMainPageData2FromRoom(type: String, mod: String): LiveData<List<DocumentInfoWithKnowledge>>{
        return docRep.getDocsInfoByTypeFull(type, mod)
    }
    fun getFilteredDocumentsFromRoom(type: String, mod: String, docPres: DocumentPreference): LiveData<List<DocumentInfoWithKnowledge>>{
        return docRep.getDocsInfoFiltered(type, mod, docPres)
    }
    fun getFilteredDocumentsFromServ(userId: Int, type: String, mod: String, docPres: DocumentPreference): LiveData<String>{
        return docRep.getDocumentFilterableServ(userId, type, mod, docPres)
    }
    fun getRespDocsFromRoom(type: String): LiveData<List<DocumentInfoWithKnowledge>>{
        return docRep.getDocsInfoByUserRespType(type)
    }
    fun getRespDocsFromServ(type: String, userId: Int): LiveData<String>{
        return docRep.getRespDocsFromServ(type, userId)
    }
    fun getUserRespFromRoom(userId: Int): LiveData<List<ResponseInfo>>{
        return docRep.getUserRespInfo(userId)
    }
    fun getUserRespWithDocInfoFromServ(userId: Int, num: Int): LiveData<String>{
        return docRep.getUserRespFromServ(userId, num)
    }

    fun getUserDocumentsFromRoom(userId: Int): LiveData<List<DocumentInfoWithKnowledge>>{
        return docRep.getDocsInfoByUserId(userId)
    }

//    fun getMainPageDataSmallInfoFromRoom(type: String, mod: String): LiveData<List<DocumentInfoForMainView>>{
//        return docRep.getDocsInfoByTypeKnow(type)
//    }
    fun getDocumentInfoRoom(id: Int): LiveData<DocumentInfoWithKnowledge>{
        return docRep.getDocumentById(id)
    }
    fun getDocumentTitleAndUserName(docId: Int, userId: Int): LiveData<ChatTitleAndAnotherUserNick>{
        return docRep.getTitleAndUserNick(docId, userId)
    }
    fun getCommentsFromRoom(id: Int): LiveData<List<Comment>>{
        return commRep.getCommentsByRespId(id)
    }
    fun getCommentFromRoom(commId: Int): LiveData<Comment>{
        return commRep.getCommentById(commId)
    }
    fun getCommentsFromServ(userId: Int, respId: Int): LiveData<List<Comment>>{
        return commRep.getCommentsFromServ(userId, respId)
    }
    fun getUnRegMainData(type: String, num: Int): String{
        val result = docRep.getUnRegStartedDataFromServ(type, num)
        Toast.makeText(getApplication(),result, Toast.LENGTH_SHORT).show()
        return result
    }

    fun getRegMainData(id: Int, type: String, num: Int, startNum: Int, mod: String){
        val result = docRep.getRegStartedDataFromServ(id, type, num, startNum, mod)
//
//        Toast.makeText(getApplication(),result, Toast.LENGTH_SHORT).show()
//        return result
    }
//    fun getRegExpTimeData(id: Int, type: String, num: Int): LiveData<String>{
//        val result = docRep.getExperienceTimeFromServ()
//        return result
//    }
    fun addCommentToRoom(comment: Comment){
        return commRep.postCommentToRoom(comment)
    }
    fun sendMessageToServ(userId: Int, content: String,  respId: Int, date: String): LiveData<PostResponse>{
        return commRep.postCommentToServ(token, content, respId, userId, date)
    }
    fun putComment(commId: Int, content: String,  userId: Int): LiveData<CommWithResp>{
        return commRep.putCommentToServ(token, commId, content,  userId)
    }
    fun putCommentToRoom(commId: Int, content: String, status: String): LiveData<String> {
        return commRep.updateCommById(commId, content, status)
    }
    fun deleteCommentRequest(commId: Int, userId: Int): LiveData<String> {
        val item =  commRep.deleteCommentByIdFromServ(token, commId, userId)
        return item
    }



}