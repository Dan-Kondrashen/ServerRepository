package ru.kondrashen.diplomappv20.repository.repositories

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.kondrashen.diplomappv20.repository.data_class.Role
import kotlin.coroutines.CoroutineContext
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.RoleDAO


class AuthRoleRepository(private val roleDAO: RoleDAO): CoroutineScope {
    private val roleAPI = APIFactory.roleApi
    private var roles: LiveData<List<String>> = roleDAO.getRoleNames()

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job

    private fun getDataFromServer() {
        launch(Dispatchers.IO) {
            val resp = roleAPI.getRolesAsync() as MutableList<Role>
            for (i in resp)
                roleDAO.addItem(i)
        }
    }

    fun clearUserData(){
        runBlocking(Dispatchers.IO) {
            roleDAO.deleteAllData()
        }
    }

    fun getAllRolesName(): LiveData<List<String>> {
        getDataFromServer()
        return roles
    }

    fun getRoleId(name: String): LiveData<Int>{
        return roleDAO.getRoleId(name)
    }

    fun getRoleNameById(id: Int): LiveData<String>{
        return roleDAO.getRoleNameById(id)
    }


//    fun postLoginData(userLog: UserLog): AuthResponse {
//        runBlocking {
//            resp = userAPI.postLoginData(userLog)
//        }
//        return resp
//    }
}