package ru.kondrashen.diplomappv20.repository.api

import retrofit2.http.GET
import ru.kondrashen.diplomappv20.repository.data_class.Role

interface RoleAPI {
    @GET("roles")
    suspend fun getRolesAsync(): List<Role>
}