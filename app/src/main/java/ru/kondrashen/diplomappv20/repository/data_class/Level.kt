package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_table")
data class Level(
    @PrimaryKey
    var id: Int,
    var number: Int,
    var minPoints: Int,
    var maxPoints: Int
)
