package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef

data class DocumentInfoWithKnowledge (

    @Embedded val document: DocumentInfo,
    @Relation(parentColumn = "docId",
        entityColumn = "knowId",
        associateBy = Junction(KnowledgeToDocumentCrossRef::class)
    )
    val knowledge: List<Knowledge>,

    @Relation(parentColumn = "userId",
    entityColumn = "specId",
    associateBy = Junction(DocDependencies::class)
    )
    val specializations: List<Specialization>,

    @Relation(parentColumn = "userId",
        entityColumn = "expId"
    )
    val experience: List<Experience>,


    @Relation(parentColumn = "docId",
        entityColumn = "docId",
    )
    val docResponse: List<DocResponse>
)
