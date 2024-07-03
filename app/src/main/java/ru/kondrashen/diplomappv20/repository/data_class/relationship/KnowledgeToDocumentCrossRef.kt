package ru.kondrashen.diplomappv20.repository.data_class.relationship

import androidx.room.Entity

@Entity(primaryKeys = ["docId","knowId"])
data class KnowledgeToDocumentCrossRef(
    val docId: Int,
    val knowId: Int
)