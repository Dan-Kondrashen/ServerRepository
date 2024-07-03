package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import android.icu.text.CaseMap.Title

data class SendMessageDto (
    val to: String?,
    val notification: NotifivationBody
)
data class NotifivationBody(
    val title: String,
    val body: String
)