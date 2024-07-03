package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class DocumentInfoForMainView (
    var id: Int,
    var title: String,
    var salaryF: Float,
    var salaryS: Float,
    var type: String,
    var userFIO: String,
    var date: String,
    var numViews: Int?,
//    var status: String,
//    var experience: String
    var knowledgeInfo: String?
)