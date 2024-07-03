package ru.kondrashen.diplomappv20.repository.api

import androidx.lifecycle.LiveData
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddUser
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLog
import ru.kondrashen.diplomappv20.repository.responces.ServerResponse
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.CallUserListPref
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UpdateUser
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserTokenAndId
import ru.kondrashen.diplomappv20.repository.responces.AuthResponse

interface UsersAPI {
    @GET("users")
    suspend fun getUsersAsync(): List<User>
    @GET("users/{user_id}")
    suspend fun getUserAsync(@Path("user_id") id: Int): Response<JsonObject>
    @GET("admin/{auth_user_id}/users/{user_id}")
    suspend fun getUserAdminAsync(@Path("user_id") uId: Int, @Path("user_id") id: Int): Response<JsonObject>
    @POST("admin/{auth_user_id}/users")
    suspend fun getUsersListAdminAsync(@Header("Authorization") token: String?, @Path("auth_user_id") uId: Int, @Body preference: CallUserListPref): Response<List<User>>
    @POST("users/{user_id}/token")
    suspend fun postUserTokenAsync(@Path("user_id") id: Int, @Body updateTokenRequest: UserTokenAndId)
    @PUT("users/{auth_user_id}/searchable-user/{user_id}")
    suspend fun putUserAsync(@Header("Authorization") token: String?,
                             @Path("auth_user_id") id: Int,
                             @Path("user_id") uId: Int,
                             @Body putUserRequest: UpdateUser): Response<JsonObject>
    @POST("users/registration")
    suspend fun postUserAsync(@Body postUserRequest: AddUser): AuthResponse
    @POST("users/login")
    suspend fun postLoginData(@Body userLog: UserLog): AuthResponse
//    @PUT("users/{user_id}")
//    suspend fun putUserAsync(@Path("user_id") id: Int, @Body putUserRequest: User): ServerResponse
    @DELETE("users/{user_id}")
    suspend fun  deleteUserAsync(@Path("user_id") id: Int): ServerResponse

    @DELETE("users/{user_id}/documents/{doc_id}")
    suspend fun  deleteUserDocumentById(@Header("Authorization") token: String?, @Path("user_id") userId: Int, @Path("doc_id") docId: Int): Response<JsonObject>
    @DELETE("users/{user_id}/responses/{resp_id}")
    suspend fun  deleteUserResponseById(@Header("Authorization") token: String?, @Path("user_id") userId: Int, @Path("resp_id") docId: Int): Response<JsonObject>
}