package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "know_to_user_table")
data class KnowledgeToUser(
    @PrimaryKey(autoGenerate = true)
    var id: Int =0,
    var knowId: Int,
    var userId: Int
)