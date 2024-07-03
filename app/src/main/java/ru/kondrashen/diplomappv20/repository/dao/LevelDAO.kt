package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Level
import ru.kondrashen.diplomappv20.repository.data_class.UserLevel
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLevelInfoForProgressView

@Dao
interface LevelDAO: BaseDAO<Level> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserLevel(item: UserLevel)

    @Query("SELECT  user_level_table.id, curPoints,level_table.maxPoints, level_table.minPoints, " +
            "user_level_table.levelId, level_table.number, user_level_table.userId, " +
            "prev_level.number as prevNumber, " +
            "next_level.number as nextNumber FROM user_level_table " +
            "JOIN level_table ON level_table.id = user_level_table.levelId " +
            "LEFT JOIN level_table AS prev_level ON prev_level.number = level_table.number - 1\n" +
            "LEFT JOIN level_table AS next_level " +
            "ON next_level.number = level_table.number + 1 WHERE userId =:userId")
    fun getUserLevelInfo(userId: Int): LiveData<UserLevelInfoForProgressView>

    @Query("SELECT * FROM level_table WHERE number = :levelNum")
    fun gteLevelInfo(levelNum: Int): LiveData<Level>
}