package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.ExperienceTime
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.responces.ServerResponse

interface ExperienceAPI {
    @GET("experience/time")
    suspend fun getExperienceTimeAsync(): Response<List<ExperienceTime>>
    @GET("experience/{exp_id}")
    suspend fun getExperienceAsync(@Path("exp_id") id: Int): Experience

    @GET("users/{auth_user_id}/experience/searchable-user/{user_id}")
    suspend fun getUserExperienceAsync(@Path("auth_user_id") curUserId: Int,
                                       @Path("user_id") uId: Int): Response<List<Experience>>

    @GET("users/{auth_user_id}/searchable-user/{user_id}/level-exp")
    suspend fun getUserAppExpAsync(@Header("token")token: String,
                                   @Path("auth_user_id") curUserId: Int,
                                   @Path("user_id") uId: Int): Response<List<UserExperience>>

    @Multipart
    @POST("users/{auth_user_id}/searchable-user/{user_id}/level-exp")
    suspend fun postUserAppExpAsync(@Header("token")token: String,
                                    @Path("auth_user_id") curUserId: Int,
                                    @Path("user_id") uId: Int,
                                    @Part("name") name: RequestBody,
                                    @Part("type") type: RequestBody,
                                    @Part("reason") reason: RequestBody,
                                    @Part("status") status: RequestBody,
                                    @Part("points") points: RequestBody,
                                    @Part part: MultipartBody.Part): Response<JsonObject>
    @Multipart
    @PUT("users/{auth_user_id}/searchable-user/{user_id}/level-exp/{exp_id}")
    suspend fun putUserAppExpAsync(@Header("token")token: String,
                                   @Path("auth_user_id") curUserId: Int,
                                   @Path("user_id") uId: Int,
                                   @Path("exp_id") expId: Int,
                                   @Part("name") name: RequestBody,
                                   @Part("type") type: RequestBody,
                                   @Part("reason") reason: RequestBody,
                                   @Part("status") status: RequestBody,
                                   @Part("points") points: RequestBody,
                                   @Part part: MultipartBody.Part): Response<JsonObject>

    @DELETE("users/{auth_user_id}/searchable-user/{user_id}/level-exp/{exp_id}")
    suspend fun deleteUserAppExpAsync(@Header("token")token: String,
                                      @Path("auth_user_id") curUserId: Int,
                                      @Path("user_id") uId: Int,
                                      @Path("exp_id") expId: Int): Response<JsonObject>

    @Multipart
    @POST("users/{auth_user_id}/experience/searchable-user/{user_id}")
    suspend fun postExpFileAuth(@Header("token")token: String,
                                @Path("auth_user_id") curUserId: Int,
                                @Path("user_id") id: Int,
                                @Part("name") name: RequestBody,
                                @Part("role") role: RequestBody,
                                @Part("place") place: RequestBody,
                                @Part("experience") experience: RequestBody,
                                @Part("exp_time_id") expTimeId: RequestBody,
                                @Part part: MultipartBody.Part): Response<JsonObject>

    @DELETE("users/{user_id}/experience/{exp_id}")
    suspend fun deleteExpFileAuth(@Header("token")token: String,
                              @Path("user_id") id: Int,
                              @Path("exp_id") expId: Int): Response<ServerResponse>

    @Multipart
    @PUT("users/{user_id}/experience/{exp_id}")
    suspend fun putExpFileAuth(@Header("token")token: String,
                                @Path("user_id") id: Int,
                                @Path("exp_id") expId: Int,
                                @Part("name") name: RequestBody?,
                                @Part("role") role: RequestBody,
                                @Part("place") place: RequestBody?,
                                @Part("experience") experience: RequestBody?,
                                @Part("exp_time_id") expTimeId: RequestBody,
                                @Part part: MultipartBody.Part?): Response<JsonObject>

}