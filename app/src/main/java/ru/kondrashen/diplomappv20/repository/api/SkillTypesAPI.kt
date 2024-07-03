package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.SkillType

interface SkillTypesAPI {

    @GET("skill-type/{type_id}/knowledges")
    suspend fun getKnowledgeBySkillTypeAsync(@Path("type_id") typeId: Int): Response<JsonObject>

    @GET("skill-type/mod/{mod}")
    suspend fun getSkillTypesAsync(@Path("mod") mod: String): Response<List<JsonObject>>
}