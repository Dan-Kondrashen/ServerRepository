package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.FullCurrentUserInfo

@Dao
interface UserDAO: BaseDAO<User> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUserArchive(item: Archive)

    @Query("DELETE FROM user_table WHERE id == :userId")
    fun deleteUserDataById(userId: Int)

    @Query("SELECT * FROM user_table ORDER BY fname DESC")
    fun getUsers(): LiveData<List<User>>

    @Query("SELECT * FROM user_table WHERE user_table.roleId != :userType AND user_table.id != :userId ORDER BY registration_date DESC")
    fun getUsersAdminSimple(userId: Int, userType :Int): LiveData<List<User>>

    @Query("SELECT * FROM user_table WHERE user_table.roleId != :userType AND user_table.id != :userId ORDER BY registration_date DESC")
    fun getUsersAdmin(userId: Int, userType :Int): LiveData<List<User>>

    @Query("UPDATE user_table SET fname = :fname, lname = :lname, mname = :mname, phone = :phone, email =:email WHERE id = :uId")
    fun putUser(uId: Int, fname: String, lname: String, mname: String?, email: String, phone: Long)

    @Query("SELECT * FROM user_table WHERE id = :uId")
    fun getUser(uId: Int): LiveData<User>

    @Query("SELECT * FROM user_table WHERE id = :uId")
    fun getCurUserAccauntInfo(uId: Int): LiveData<FullCurrentUserInfo>
}