package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "analitic_skill_table")
data class AnaliticSkill (
    @PrimaryKey
    var id: Int,
    var date: Date,
    var numUsage: Int,
    var respType: String,
    var specId: Int?,
    var knowId: Int?,
)