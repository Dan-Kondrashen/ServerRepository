package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_level_table")
data class UserLevel(
    @PrimaryKey
    var id: Int,
    var curPoints: Int,
    var levelId: Int,
    var userId: Int,
)
