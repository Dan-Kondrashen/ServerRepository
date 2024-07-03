package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.AnaliticSkill
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AnalyticFilter

interface AnalyticAPI {

    @POST("analytics-skill-info/mod/{mode}")
    suspend fun getSkillAnalysisAsync(@Path("mode") mode: String, @Body analyticFilter: AnalyticFilter): Response<List<JsonObject>>

    @POST("users/{user_id}/analytics-skill-info/mod/{mode}")
    suspend fun getUserSkillAnalysisAsync(@Path("user_id") userId: Int, @Path("mode") mode: String, @Body analyticFilter: AnalyticFilter): Response<List<AnaliticSkill>>

}