package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge

data class AnaliticWithStatus(
    var knowledge: List<Knowledge>,
    var position: Int
)
