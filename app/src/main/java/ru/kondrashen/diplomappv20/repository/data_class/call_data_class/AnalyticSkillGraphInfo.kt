package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import java.sql.Date

data class AnalyticSkillGraphInfo (
    var date: String?,
    var numUsage: Int,
    var respType: String,
    var skillName: String
)