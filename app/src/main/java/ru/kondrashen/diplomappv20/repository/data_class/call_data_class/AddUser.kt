package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

data class AddUser(
    var fname: String,
    var lname: String,
    var mname: String,
    var phone: Long?,
    var email: String,
    var password: String,
    var roleId: Long
)