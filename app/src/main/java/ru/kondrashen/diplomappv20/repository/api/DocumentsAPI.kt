package ru.kondrashen.diplomappv20.repository.api
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocument
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocumentFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocCounts
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentPreference
import ru.kondrashen.diplomappv20.repository.responces.ServerResponse

interface DocumentsAPI {
    @GET("documents/type/{type}")
    suspend fun getDocumentsAsync(@Path("type") type: String): Response<JsonObject>
    @GET("documents/{doc_id}")
    suspend fun getDocumentAsync(@Path("doc_id") id: Int): Document
    @POST("documents")
    suspend fun postDocumentAsync(@Body postDocumentRequest: AddDocument)
    @PUT("documents/{doc_id}")
    suspend fun putDocumentAsync(@Path("doc_id") id: Int, @Body putDocumentsRequest: Document): ServerResponse
    @DELETE("documents/{doc_id}")
    suspend fun deleteDocumentAsync(@Path("doc_id") id: Int)
    @GET("documents/type/{type}/items/{num}")
    suspend fun getNumDocumentsAsync(@Path("type") type: String, @Path("num") num: Int): Response<List<JsonObject>>

    @POST("users/{user_id}/documents/type/{type}/mod/{mod}")
    suspend fun getNumDocumentsForUserAsync(@Path("user_id") id: Int, @Path("type") type: String, @Path("mod") mod: String, @Body docCountInfo: DocCounts): Response<JsonObject>

    @POST("users/{user_id}/documents/type/{type}/mod/{mod}/filterable")
    suspend fun getDocumentsFilterableAsync(@Path("user_id") id: Int, @Path("type") type: String, @Path("mod") mod: String, @Body docPref: DocumentPreference): Response<JsonObject>
//    @GET("users/{user_id}/documents/type/{type}/items/{num}/mod/{mod}")
//    suspend fun getNumDocumentsForUserAsync(@Path("user_id") id: Int, @Path("type") type: String, @Path("num") num: Int, @Path("mod") mod: String): Response<JsonObject>
    @GET("users/{user_id}/responses/type/{type}")
    suspend fun getUserRespDocumentsAsync(@Path("user_id") id: Int, @Path("type") type: String): Response<List<JsonObject>>
    @GET("users/{user_id}/responses/num/{num}")
    suspend fun getUserRespAsync(@Path("user_id") id: Int, @Path("num") num: Int): Response<JsonObject>
    @POST("users/{user_id}/documents/mod/{mod}")
    suspend fun postNewUserDocumentAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("mod") mod: String,  @Body postDocumentRequest: AddDocumentFullInfo): Response<JsonObject>

    @GET("users/{user_id}/documents/mod/{mod}")
    suspend fun getUserDocumentInfoAsync(@Header("Authorization") token: String?, @Path("user_id") id: Int, @Path("mod") mod: String): Response<JsonObject>
}
