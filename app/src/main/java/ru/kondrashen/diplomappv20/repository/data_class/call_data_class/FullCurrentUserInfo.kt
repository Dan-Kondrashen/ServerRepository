package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToUserCrossRef

data class FullCurrentUserInfo (

    @Embedded val user: User,
    @Relation(parentColumn = "id",
        entityColumn = "userId",
    )
    val experience: List<Experience>,

)