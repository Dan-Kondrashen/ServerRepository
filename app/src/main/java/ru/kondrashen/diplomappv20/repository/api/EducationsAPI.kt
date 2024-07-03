package ru.kondrashen.diplomappv20.repository.api

import retrofit2.http.GET
import retrofit2.http.Path
import ru.kondrashen.diplomappv20.repository.data_class.Education


interface EducationsAPI {
    @GET("educations")
    suspend fun getEducationsAsync(): List<Education>
    @GET("educations/{edu_id}")
    suspend fun getEducationAsync(@Path("edu_id") id: Int): Education
}