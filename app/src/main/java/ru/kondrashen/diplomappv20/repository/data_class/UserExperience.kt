package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_exp_table")
data class UserExperience(
    @PrimaryKey
    var id: Int,
    var reason: String?,
    var points: Int,
    var type: String,
    var status: String,
    var userId: Int,
    var documents_scan_id: Int?
)
