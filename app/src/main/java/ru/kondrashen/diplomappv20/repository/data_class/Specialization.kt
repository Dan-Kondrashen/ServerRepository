package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "specialization_table")
data class Specialization(
    @PrimaryKey
    var specId: Int,
    var name: String,
    var description: String?
)
