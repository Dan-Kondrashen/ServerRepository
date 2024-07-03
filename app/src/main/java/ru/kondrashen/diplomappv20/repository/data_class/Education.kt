package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "education_table")
data class Education(
    @PrimaryKey
    var id: Int,
    var name: String,
    var description: String?
)
