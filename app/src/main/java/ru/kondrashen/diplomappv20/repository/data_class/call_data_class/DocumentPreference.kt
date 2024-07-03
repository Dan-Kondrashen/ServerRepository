package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import java.sql.Date

data class DocumentPreference(
    var orderType: String,
    var salaryF: Float?,
    var salaryS: Float?,
    var dateStart: String?,
    var dateEnd: String?,
    var knowIdList: List<Int>?,
    var numItems: Int?
)