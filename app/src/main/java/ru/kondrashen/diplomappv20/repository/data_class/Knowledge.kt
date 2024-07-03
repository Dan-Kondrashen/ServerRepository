package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_table")
data class Knowledge(
    @PrimaryKey
    var knowId: Int,
    var name: String,
    var description: String?
)
