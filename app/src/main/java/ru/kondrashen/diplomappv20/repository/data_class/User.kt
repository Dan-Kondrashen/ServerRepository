package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_table", indices = [Index(value=["id"], unique = true)])
data class User(
    @PrimaryKey
    var id: Int,
    var fname: String,
    var lname: String,
    var mname: String?,
    var status: String?,
    var phone: Long?,
    var email: String?,
    var roleId: Int,
    var registration_date: String
)
