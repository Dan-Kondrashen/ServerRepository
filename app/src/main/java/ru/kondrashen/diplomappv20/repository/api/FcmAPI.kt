package ru.kondrashen.diplomappv20.repository.api

import retrofit2.http.Body
import retrofit2.http.POST
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.SendMessageDto

interface FcmAPI {
    @POST("/send")
    suspend fun sendMessage(
        @Body sendMessageDto: SendMessageDto
    )
    @POST("/broadcast")
    suspend fun broadcast(
        @Body sendMessageDto: SendMessageDto
    )
}