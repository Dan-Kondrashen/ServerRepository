package ru.kondrashen.diplomappv20.repository.data_class.relationship

import androidx.room.Entity

@Entity(primaryKeys = ["id","specId"], tableName = "spec_to_edu_table")

data class SpecializationToEducationCrossRef(
    val id: Int,
    val specId: Int
)
