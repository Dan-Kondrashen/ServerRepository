package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.dao.CommentsDAO
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddArchive
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddComment

interface CommentsAPI {
    @GET("comments/{comm_id}")
    suspend fun getCommentsAsync(@Path("comm_id") id: Int): List<Comment>

    @GET("users/{user_id}/response/{resp_id}/comments")
    suspend fun getCommentsByRespIdAsync(@Path("user_id") id: Int, @Path("resp_id") respId: Int ): Response<List<Comment>>

    @POST("users/{user_id}/response/{resp_id}/comments")
    suspend fun postCommentAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("resp_id") respId: Int, @Body postCommentRequest: AddComment): Response<JsonObject>

    @PUT("users/{user_id}/comments/{comm_id}")
    suspend fun putCommentAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("comm_id") archId: Int, @Body putCommentRequest: AddComment): Response<JsonObject>

    @DELETE("users/{user_id}/comments/{comm_id}")
    suspend fun deleteCommentByIdAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("comm_id") commId: Int): Response<JsonObject>

}