package ru.kondrashen.diplomappv20.repository.data_class
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "document_table", indices = [Index(value=["docId"], unique = true)])
data class Document(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "docId") var docId: Int,
    var title: String,
    var salaryF: Float?,
    var salaryS: Float?,
    var extra_info: String,
    var contactinfo: String,
    var type: String,
    var userId: Int,
    var date: String
)