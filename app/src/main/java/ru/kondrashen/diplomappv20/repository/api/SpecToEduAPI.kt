package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToEducationCrossRef

interface SpecToEduAPI {
    @GET("specializations-to-educations")
    suspend fun getSpecToEduAsync(): Response<List<JsonObject>>

    @Multipart
    @POST("users/{user_id}/specializationToEducation")
    suspend fun postSpecToEduFileAuth(@Header("token")token: String,
                                @Path("user_id") sid: Int,
                                @Part("name") name: RequestBody?,
                                @Part("specId") specId: RequestBody,
                                @Part("eduId") eduId: RequestBody,
                                @Part part: MultipartBody.Part?): Response<JsonObject>


}