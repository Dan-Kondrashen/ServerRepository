package ru.kondrashen.diplomappv20.repository.data_class.call_data_class


data class AddDocument(
    var title: String,
    var salaryF: String,
    var salaryS: String,
    var extraInfo: String,
    var contactInfo: String,
    var type: String,
    var userId: Int,

)