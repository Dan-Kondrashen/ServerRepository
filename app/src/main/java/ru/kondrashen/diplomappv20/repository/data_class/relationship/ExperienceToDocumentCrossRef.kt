package ru.kondrashen.diplomappv20.repository.data_class.relationship

import androidx.room.Entity

@Entity(primaryKeys = ["docId","expId"])
data class ExperienceToDocumentCrossRef(
    val docId: Int,
    val expId: Int
)