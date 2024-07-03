package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dependence_table")
data class DocDependencies(
    @PrimaryKey
    var id: Int,
    var userId: Int,
    var specId: Int,
    var eduId: Int?,
    var documentsScanId: Int?

)