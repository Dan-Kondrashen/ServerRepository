package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comment_table")
data class Comment(
    @PrimaryKey
    var id: Int,
    var content: String,
    var status: String,
    var comment_date: String,
    var userId: Int,
    var respId: Int
)