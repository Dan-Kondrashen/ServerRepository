package ru.kondrashen.diplomappv20.repository.data_class.relationship

import androidx.room.Entity

@Entity(primaryKeys = ["id","specId"])
data class SpecializationToUserCrossRef(
    val id: Int,
    val specId: Int
)
