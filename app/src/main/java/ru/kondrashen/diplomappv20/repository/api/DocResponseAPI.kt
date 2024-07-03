package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocResponse
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse

interface DocResponseAPI {
    @GET("/responses")
    suspend fun getAllDocResponseAsync(): List<DocResponse>
    @POST("/responses")
    suspend fun postDocResponseAsync(@Body postDocResponse: AddDocResponse)
    @POST("/users/{user_id}/responses")
    suspend fun postDocResponseListAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Body postDocResponse: JsonObject): Response<JsonObject>
}