package ru.kondrashen.diplomappv20.repository.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge

interface KnowledgesAPI {

    @GET("skill-type/{type_id}/knowledges")
    suspend fun getKnowledgeBySkillTypeAsync(@Path("type_id") typeId: Int): Response<List<Knowledge>>
    @GET("knowledges/mod/{mod}")
    suspend fun getKnowledgeAsync(@Path("mod") mod: String): Response<List<Knowledge>>
}