package ru.kondrashen.diplomappv20.repository.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.Specialization

interface SpecializationsAPI {
    @GET("specializations")
    suspend fun getSpecsAsync(): List<Specialization>

    @GET("specializations/mod/{mod}")
    suspend fun getSpecsModAsync(@Path("mod") mod: String): Response<List<Specialization>>

    @GET("specializations/{spec_id}")
    suspend fun getSpecAsync(@Path("spec_id") id: Int): Specialization

    @GET("skill-type/{typeId}/specializations")
    suspend fun getSpecializationBySkillTypeAsync(@Path("typeId") typeId: Int): Response<List<Specialization>>

}