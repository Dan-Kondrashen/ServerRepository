package ru.kondrashen.diplomappv20.repository.data_class.relationship

import androidx.room.Entity

@Entity(primaryKeys = ["id","knowId"], tableName = "knowledge_type_cross_ref")
data class KnowledgeTypeCrossRef(
    val id: Int,
    val knowId: Int
)