package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skill_type_table")
data class SkillType(
    @PrimaryKey
    var id: Int,
    var name: String,
    var description: String?
)
