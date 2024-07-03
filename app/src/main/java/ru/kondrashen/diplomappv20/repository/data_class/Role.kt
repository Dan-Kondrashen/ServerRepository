package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "role_table")
data class Role (
    @PrimaryKey
    var id: Int,
    var name: String,
    var desc: String
)