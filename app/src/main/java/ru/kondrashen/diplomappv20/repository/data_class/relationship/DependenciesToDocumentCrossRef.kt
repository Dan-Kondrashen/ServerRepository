package ru.kondrashen.diplomappv20.repository.data_class.relationship

import androidx.room.Entity

@Entity(primaryKeys = ["docId","id"], tableName = "dependencies_to_document")
data class DependenciesToDocumentCrossRef(
    val id: Int,
    val docId: Int,
)