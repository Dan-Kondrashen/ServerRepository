package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.Role

@Dao
interface RoleDAO: BaseDAO<Role> {
    @Query("SELECT * FROM role_table ORDER BY name DESC")
    fun getUsers(): LiveData<List<Role>>

    @Query("SELECT name FROM role_table ORDER BY name DESC")
    fun getRoleNames(): LiveData<List<String>>

    @Query("SELECT name FROM role_table WHERE id = :roleId")
    fun getRoleNameById(roleId: Int): LiveData<String>

    @Query("SELECT id FROM role_table WHERE name = :name")
    fun getRoleId(name: String): LiveData<Int>
}