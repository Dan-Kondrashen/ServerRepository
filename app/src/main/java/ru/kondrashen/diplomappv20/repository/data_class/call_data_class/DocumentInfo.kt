package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class DocumentInfo (
    var docId: Int,
    var title: String,
    var salaryF: Float,
    var salaryS: Float,
    var type: String,
    var userFIO: String,
    var extraName: String?,
    var date: String,
    var numViews: Int?,
    var userId: Int,
    var extra_info: String?,
    var contact_info: String?
//    var status: String,
//    var experience: String
)