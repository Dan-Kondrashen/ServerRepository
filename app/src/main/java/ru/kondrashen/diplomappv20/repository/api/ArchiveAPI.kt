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
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddArchive
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddUser

interface ArchiveAPI {
    @GET("users/{user_id}/archives")
    suspend fun getArchivesAsync(@Path("user_id") id: Int): Response<List<Archive>>

    @POST("users/{user_id}/archives/")
    suspend fun postArchiveAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Body postArchiveRequest: AddArchive): Response<JsonObject>

    @GET("users/{user_id}/archives/{arch_id}")
    suspend fun getArchiveByIdAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("arch_id") archId: Int): Response<Archive>

    @PUT("users/{user_id}/archives/{arch_id}")
    suspend fun putArchiveAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("arch_id") archId: Int, @Body putArchiveRequest: AddArchive): Response<JsonObject>

    @DELETE("users/{user_id}/archives/{arch_id}")
    suspend fun deleteArchiveByIdAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("arch_id") archId: Int): Response<JsonObject>
}