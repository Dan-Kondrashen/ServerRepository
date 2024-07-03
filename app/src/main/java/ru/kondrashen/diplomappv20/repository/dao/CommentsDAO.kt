package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import java.sql.Date

@Dao
interface CommentsDAO: BaseDAO<Comment> {
    @Query("SELECT * FROM comment_table ORDER BY comment_date ASC")
    fun getComments(): LiveData<List<Comment>>
    @Query("SELECT * FROM comment_table WHERE id == :commId ORDER BY comment_date ASC")
    fun getCommentById(commId: Int): LiveData<Comment>

    @Query("UPDATE comment_table SET content = :content, status= :status, comment_date = :date WHERE id = :commId")
    fun updateCommentById(commId: Int,  content: String, status: String, date: String)

    @Query("UPDATE comment_table SET content = :content, status= :status WHERE id = :commId")
//    fun updateCommentById(commId: Int,  content: Strient WHERE id = :commId")
    fun updateCommentContentById(commId: Int,  content: String, status: String)

    @Query("UPDATE comment_table " +
            "SET id = :commId, comment_date = :trueDate, status = :status " +
            "WHERE comment_date == :timestamp  AND userId = :userId AND id == 0")
    fun updateCommentByTimestamp(commId: Int,  userId: Int, trueDate: String, status: String,timestamp: String)

    @Query("SELECT * FROM comment_table WHERE respId == :respId  ORDER BY comment_date ASC")
    fun getCommentsWithRespIDs(respId: Int): LiveData<List<Comment>>

    @Query("SELECT * FROM comment_table WHERE userId == :userId ORDER BY comment_date DESC")
    fun getCommentsWithUserIDs(userId: Int): LiveData<List<Comment>>

    @Query("DELETE FROM comment_table WHERE id = :commId")
    fun deleteCommentByIdFromRoom(commId: Int)

    @Query("DELETE FROM comment_table WHERE id = :commId AND comment_date == :timestamp")
    fun deleteCommentByIdAndStumpFromRoom(commId: Int, timestamp: String)

}