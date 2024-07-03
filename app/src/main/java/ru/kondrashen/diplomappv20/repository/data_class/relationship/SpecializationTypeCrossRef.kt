package ru.kondrashen.diplomappv20.repository.data_class.relationship

import androidx.room.Entity

@Entity(primaryKeys = ["id","specId"], tableName = "specialization_type_cross_ref")
data class SpecializationTypeCrossRef(
    val id: Int,
    val specId: Int
)