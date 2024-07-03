package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class UserLevelInfoForProgressView (
    var id: Int,
    var curPoints: Int,
    var minPoints: Int,
    var maxPoints: Int,
//    var levelId: Int,
    var number: Int,
    var userId: Int,
    var prevNumber: Int,
    var nextNumber: Int,
)