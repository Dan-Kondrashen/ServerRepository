package ru.kondrashen.diplomappv20.domain

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddUser
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLog
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.db.WorkSearcherDB
import ru.kondrashen.diplomappv20.repository.repositories.AuthRoleRepository
import ru.kondrashen.diplomappv20.repository.repositories.AuthUserRepository
import ru.kondrashen.diplomappv20.repository.responces.AuthResponse

class AuthViewModel(application: Application): AndroidViewModel(application) {
    private val roleRep: AuthRoleRepository
    private val userRep: AuthUserRepository
    private var roles: LiveData<List<String>>
    val pref = application.getSharedPreferences("AuthPref", Context.MODE_PRIVATE)
    val editor = pref.edit()

    private lateinit var response: AuthResponse

    init {
        val roleDao = WorkSearcherDB.getDatabase(application).authDopDao()
        val docDao = WorkSearcherDB.getDatabase(application).docDao()
        val authUserDao = WorkSearcherDB.getDatabase(application).authDao()
        val archiveDao = WorkSearcherDB.getDatabase(application).archiveDao()
        val levelDao = WorkSearcherDB.getDatabase(application).levelDao()
        roleRep = AuthRoleRepository(roleDao)
        userRep =  AuthUserRepository(authUserDao, docDao, archiveDao, levelDao)
        roles = roleDao.getRoleNames()
    }
    fun getRolesFromServer(): LiveData<List<String>> {
        roles = roleRep.getAllRolesName()
        return roles
    }

    fun getRolesId(name: String): LiveData<Int> {
        return roleRep.getRoleId(name)
    }



    fun clearDatabase(){
        roleRep.clearUserData()
    }

    fun getRolesFromRoom(): LiveData<List<String>> {
        return roles
    }
    fun getCurrentUser(userId: Int): LiveData<User> {
        return userRep.getUser(userId)
    }
    fun login(log: UserLog): AuthResponse{
        response = userRep.postLogData(log)
        editor.putString("token", response.accessToken)
        editor.apply()
        return  response
    }
    fun register(user: AddUser): AuthResponse{
        response = userRep.postRegData(user)
        editor.putString("token", response.accessToken)
        editor.apply()
        return  response
    }
}
