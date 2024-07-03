package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exp_time_table")
data class ExperienceTime(
    @PrimaryKey
    var id: Int,
    var experienceTime: String
)