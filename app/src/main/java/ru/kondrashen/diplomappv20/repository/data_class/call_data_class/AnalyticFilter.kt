package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class AnalyticFilter(
    val endDate: String,
    val startDate: String,
    val skillId: Int?,
    val skillType: String,
    val skillFamilyId: Int?
)
