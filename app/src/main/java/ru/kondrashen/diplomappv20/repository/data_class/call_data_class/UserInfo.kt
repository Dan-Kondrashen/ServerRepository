package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import androidx.room.Entity
import androidx.room.PrimaryKey
data class UserInfo(
    var id: Int,
    var fname: String,
    var lname: String,
    var mname: String?,
    var phone: Long?,
    var email: String?,
    var role: String,
    var registration_date: String
)
