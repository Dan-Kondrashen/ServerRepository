package ru.kondrashen.diplomappv20.repository.data_class

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_views_table")
data class Views (
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val docId: Int,
    @ColumnInfo(name = "typeS", defaultValue = "view") val typeS: String,
    val numviews: Int
)