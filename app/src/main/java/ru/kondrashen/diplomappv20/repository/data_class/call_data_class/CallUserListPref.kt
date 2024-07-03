package ru.kondrashen.diplomappv20.repository.data_class.call_data_class


data class CallUserListPref(
    var mod: String,
    var info: String?,
    var roleIDs: List<Int>?,
    var status: String?,
    var num: Int?,
    var modUserInfo: String?

)