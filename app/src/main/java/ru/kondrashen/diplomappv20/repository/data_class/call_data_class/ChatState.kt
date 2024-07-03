package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class ChatState(
    var isEnteringToken: Boolean = true,
    var remoteToken: String = "",
    var content: String =""
)