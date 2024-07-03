package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.responces.ServerResponse


interface DocDependenciesAPI {
    @GET("documents/{doc_id}/dependencies")
    suspend fun getDependenciesAsync(@Path("doc_id") id: Int): List<DocDependencies>
//    @POST("documents/{doc_id}/dependencies")
//    suspend fun postDependenciesAsync(@Path("doc_id") id: Int, @Body postDependencies: AddDocDependencies): ServerResponse
//    @PUT("documents/{doc_id}/dependencies/{dep_id}")
//    suspend fun putDependenciesAsync(@Path("doc_id") id: Int, @Path("dep_id") depId: Int,@Body putDependencies: AddDocDependencies): ServerResponse

    @GET("users/{user_id}/dependencies")
    suspend fun getUserDependenciesAsync(@Path("user_id") id: Int): Response<List<DocDependencies>>

    @HEAD("users/{user_id}/files/{file_id}/download")
    suspend fun getFileMimeAsync(@Header("token")token: String,
                                 @Path("user_id") sid: Int,
                                 @Path("file_id") id: Int): Response<Void>

    @Multipart
    @POST("users/{user_id}/dependencies")
    suspend fun postDependenciesAsync(@Header("token")token: String,
                                      @Path("user_id") sid: Int,
                                      @Part("name") name: RequestBody?,
                                      @Part("specId") specId: RequestBody,
                                      @Part("eduId") eduId: RequestBody,
                                      @Part part: MultipartBody.Part?): Response<JsonObject>

    @Multipart
    @PUT("users/{user_id}/dependencies/{depend_id}")
    suspend fun putDependenciesFileAuth(@Header("token")token: String,
                                     @Path("user_id") id: Int,
                                     @Path("depend_id") dependId: Int,
                                     @Part("name") name: RequestBody?,
                                     @Part("specId") specId: RequestBody,
                                     @Part("eduId") place: RequestBody?,
                                     @Part part: MultipartBody.Part?): Response<JsonObject>

    @DELETE("users/{user_id}/dependencies/{depend_id}")
    suspend fun deleteDependenciesFileAuth(@Header("token")token: String,
                                        @Path("user_id") id: Int,
                                        @Path("depend_id") dependId: Int): Response<ServerResponse>
}