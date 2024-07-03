package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class AddDocResponse (
    var type: String,
    var userId: Int,
    var docId: Int,
    var status: String,
)