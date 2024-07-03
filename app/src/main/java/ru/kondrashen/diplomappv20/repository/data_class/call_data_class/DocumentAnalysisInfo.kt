package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.Views
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef

data class DocumentAnalysisInfo (
    @Embedded val document: Document,
    @Relation(parentColumn = "docId",
        entityColumn = "docId",
    )
    val views: List<Views>,

    @Relation(parentColumn = "docId",
        entityColumn = "docId",
    )
    val docResponse: List<DocResponse>
)
