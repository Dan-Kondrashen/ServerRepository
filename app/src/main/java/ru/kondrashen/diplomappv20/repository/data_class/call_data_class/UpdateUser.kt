package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class UpdateUser(
    var id: Int,
    var fname: String,
    var lname: String,
    var mname: String?,
    var phone: Long?,
    var email: String,
    var status: String?,
)