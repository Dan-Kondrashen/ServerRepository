package ru.kondrashen.diplomappv20.repository.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.coroutines.CoroutineContext
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.CommentsDAO
import ru.kondrashen.diplomappv20.repository.dao.UserDAO
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddComment
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ChatState
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.CommWithResp
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.NotifivationBody
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.SendMessageDto
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserTokenAndId
import ru.kondrashen.diplomappv20.repository.responces.PostResponse
import java.io.IOException


class CommentsRepository(private var commentDAO: CommentsDAO, private var userDAO: UserDAO): CoroutineScope {


    private val commentsAPI = APIFactory.commentApi
    private val userAPI = APIFactory.userApi
    private val fcmAPI = APIFactory.FcmApi
    companion object{
        private const val TAG = "CommentRepository"
    }
    private lateinit var stringResp: MutableLiveData<String>
    private lateinit var customResp: MutableLiveData<PostResponse>
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job

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
    fun sendMessage(isBroadcast: Boolean, state: ChatState): LiveData<String>{
        val content = MutableLiveData(state.content)
        launch {
            val messageDto = SendMessageDto(
                to = if (isBroadcast) null else state.remoteToken,
                notification = NotifivationBody(
                    title = "New",
                    body = state.content
                )
            )
            try {
                if (isBroadcast){
                    fcmAPI.broadcast(messageDto)
                }
                else{
                    fcmAPI.sendMessage(messageDto)
                }
                content.postValue("")

            }
            catch (e: HttpException){
                e.printStackTrace()

            }
            catch (e: IOException){
                e.printStackTrace()
            }
        }
        return content
    }
    fun getCommentsByRespId(respId: Int): LiveData<List<Comment>>{
        return commentDAO.getCommentsWithRespIDs(respId)
    }
    fun getCommentById(commId: Int): LiveData<Comment>{
        return commentDAO.getCommentById(commId)
    }
    fun getCommentsByUserId(userId: Int): LiveData<List<Comment>>{
        return commentDAO.getComments()
    }
    fun getCommentsFromServ(userId: Int, respId: Int): LiveData<List<Comment>>{
        var comments = MutableLiveData<List<Comment>>()
        launch(Dispatchers.IO) {
            try {
                val resp = commentsAPI.getCommentsByRespIdAsync(userId, respId)
                if (resp.isSuccessful) {
                    resp.body()?.let {
                        println(it+"Комментарии")
                        commentDAO.addItems(*it.toTypedArray())
                        comments.postValue(it)

                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("Chat","No connection to server")

            }

        }
        return comments
    }
    private fun putCommentToRoom(comm: Comment){
        launch (Dispatchers.IO){
            commentDAO.updateItem(comm)
        }
    }

    fun postCommentToRoom(comm: Comment){
        launch (Dispatchers.IO){
            commentDAO.addItem(comm)
        }
    }
    fun updateCommById(commId: Int, content: String, status: String): LiveData<String>{
        var str = MutableLiveData("not")
        launch (Dispatchers.IO) {
            commentDAO.updateCommentContentById(commId, content, status)
            str.postValue("done")
        }
        return str
    }
    fun postCommentToServ(token: String?, content: String, respId: Int, userId: Int, timestamp: String):LiveData<PostResponse>{
        val resultToken = "Bearer $token"
        customResp = MutableLiveData<PostResponse>()
        launch {
            try{
                val resp = commentsAPI.postCommentAsync(resultToken, userId, respId, AddComment(content, userId))
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    val result = respBody?.get("status")?.asString
                    val commId = respBody?.get("commId")?.asInt
                    val comm_date = respBody?.get("comm_date")?.asString
                    val status = respBody?.get("statusComm")?.asString
                    customResp.postValue(PostResponse(result?: "No connection to server", commId))
                    if(commId != null && result == "success") {
//                        postCommentToRoom(Comment(commId, content, "new", comm_date!!, userId, respId))
                        println(timestamp+"data2211")
                        commentDAO.deleteCommentByIdAndStumpFromRoom(0, timestamp)
                        println(comm_date+ "djn nr")
                        commentDAO.addItem(Comment(commId, content, "new", comm_date!!, userId, respId))
//                        commentDAO.updateCommentByTimestamp(
//                            commId,
//                            userId,
//                            comm_date!!,
//                            status!!,
//                            timestamp
//                        )
                    }
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

    fun putCommentToServ(token: String?, commId: Int, content: String, userId: Int):LiveData<CommWithResp>{
        val resultToken = "Bearer $token"
//        stringResp = MutableLiveData<String>()
        var stringResp = MutableLiveData<CommWithResp>()
        launch {
            try{
                val resp = commentsAPI.putCommentAsync(resultToken, userId, commId, AddComment(content, userId))
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    val result = respBody?.get("status")?.asString
                    val status = respBody?.get("statusComm")?.asString
                    val comm_date = respBody?.get("comm_date")?.asString
                    stringResp.postValue(CommWithResp(result?: "No response", status?: "Not posted"))
//                    putCommentToRoom(Comment(commId, content, status!!, comm_date!!, userId, respId))
//                    commentDAO.updateCommentById(commId, content, status!!, comm_date!!)
                }
                else
                    stringResp.postValue(CommWithResp("Server Response Code ${resp.code()}", "Not posted"))
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue(CommWithResp("No connection to server", "Not posted"))
            }
        }

        return stringResp
    }
    fun deleteCommentByIdFromServ(token: String?, commId: Int, userId: Int): LiveData<String> {
        val resultToken = "Bearer $token"
        stringResp = MutableLiveData<String>()
        launch (Dispatchers.IO){
            try{
                val resp = commentsAPI.deleteCommentByIdAsync(resultToken, userId, commId)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    var result = respBody?.get("status")?.asString
                    stringResp.postValue(result?: "No connection to server")
                    deleteCommentByIdFromRoom(commId)
                }
                else if(resp.code() == 404) {
                    deleteCommentByIdFromRoom(commId)
                    stringResp.postValue("Item not found ${resp.code()}")
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
    private fun deleteCommentByIdFromRoom(commId: Int){
        commentDAO.deleteCommentByIdFromRoom(commId)
    }
////    fun deleteDocRespFromRoom(docRespId: Int){
////        archiveDAO.deleteArchiveRespByArchId(docRespId)
////    }
}