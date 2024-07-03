package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "document_response_table", indices = [Index(value=["id"], unique = true)])
data class DocResponse (
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    var type: String,
    var userId: Int,
    var docId: Int,
    var status: String?,
)