package ru.kondrashen.diplomappv20.repository.data_class.call_data_class

import androidx.room.Embedded
import androidx.room.Relation
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Specialization

data class DocDependenceFullInfo(
    @Embedded val docDependence: DocDependencies,
    @Relation(parentColumn = "eduId",
        entityColumn = "id",)
    val educations: Education?,
    @Relation(parentColumn = "specId",
        entityColumn = "specId",)
    val specializations: Specialization
)
