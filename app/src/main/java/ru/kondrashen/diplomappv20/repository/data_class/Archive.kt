package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archive_table")
data class Archive (
    @PrimaryKey
    var id: Int,
    var name: String,
    var searchableWord: String,
    var userId: Int,
)