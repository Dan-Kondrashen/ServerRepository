package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exp_table")
data class Experience(
    @PrimaryKey
    var expId: Int,
    var expTimeId: Int,
    var role: String?,
    var experience: String?,
    var place: String?,
    var userId: Int,
    var documentScanId: Int?
)
