package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

data class PutUser(
    var id: Int,
    var fname: String,
    var lname: String,
    var mname: String?,
    var phone: Long?,
    var email: String?
)
