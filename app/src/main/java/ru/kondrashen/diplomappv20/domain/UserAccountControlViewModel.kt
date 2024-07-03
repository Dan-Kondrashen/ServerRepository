package ru.kondrashen.diplomappv20.domain

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.kondrashen.diplomappv20.repository.responces.PostResponse
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocumentFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UpdateUser
import ru.kondrashen.diplomappv20.repository.db.WorkSearcherDB
import ru.kondrashen.diplomappv20.repository.repositories.ArchiveRepository
import ru.kondrashen.diplomappv20.repository.repositories.AuthRoleRepository
import ru.kondrashen.diplomappv20.repository.repositories.AuthUserRepository
import ru.kondrashen.diplomappv20.repository.repositories.DocDependenciesRepository
import ru.kondrashen.diplomappv20.repository.repositories.DocResponsesRepository
import ru.kondrashen.diplomappv20.repository.repositories.ExperienceRepository
import ru.kondrashen.diplomappv20.repository.repositories.SpecializationToEducationRepository

class UserAccountControlViewModel(application: Application): AndroidViewModel(application) {
    private val userRep: AuthUserRepository
    private val roleRep: AuthRoleRepository
    private val archiveRep: ArchiveRepository
    private val dependenceRep: DocDependenciesRepository
    private val experienceRep: ExperienceRepository
    private val specToEduRep: SpecializationToEducationRepository
    private val reponseRep: DocResponsesRepository
    val pref = application.getSharedPreferences("AuthPref", Context.MODE_PRIVATE)
    val editor = pref.edit()
    private val token = pref.getString("token", null)

    init {
        val docDao = WorkSearcherDB.getDatabase(application).docDao()
        val docDepDao = WorkSearcherDB.getDatabase(application).docDependDao()
        val docViewDao = WorkSearcherDB.getDatabase(application).docViewDao()
        val expDao = WorkSearcherDB.getDatabase(application).expDao()
        val roleDao = WorkSearcherDB.getDatabase(application).authDopDao()
        val expTimeDao = WorkSearcherDB.getDatabase(application).expTimeDao()
        val docRespDao = WorkSearcherDB.getDatabase(application).docResponseDao()
        val specDao = WorkSearcherDB.getDatabase(application).specDao()
        val eduDao = WorkSearcherDB.getDatabase(application).eduDao()
        val analyticDao = WorkSearcherDB.getDatabase(application).analyticDao()
        val archiveDao = WorkSearcherDB.getDatabase(application).archiveDao()
        val userDao = WorkSearcherDB.getDatabase(application).authDao()
        val levelDao = WorkSearcherDB.getDatabase(application).levelDao()
        experienceRep = ExperienceRepository(expDao, expTimeDao)
        dependenceRep = DocDependenciesRepository(docDao, docDepDao, docViewDao)
        specToEduRep = SpecializationToEducationRepository(specDao, eduDao, analyticDao)
        userRep =  AuthUserRepository(userDao, docDao, archiveDao, levelDao)
        roleRep = AuthRoleRepository(roleDao)
        reponseRep = DocResponsesRepository(docRespDao, docViewDao)
        archiveRep =  ArchiveRepository(archiveDao)
    }
//    fun getCurrentUserData(userId: Int): LiveData<FullCurrentUserInfo> {
//        return userRep.getCurUser(userId)
//    }
//    fun getCurrentUserDataFromServ(userId: Int): LiveData<String> {
//        return userRep.getUserFromServ(userId)
//    }

    fun getUserArchivesRoom(userId: Int): LiveData<List<Archive>> {
        return userRep.getUserArchives(userId)
    }
    fun getUserArchivesStaticRoom(userId: Int): List<Archive> {
        return userRep.getUserArchivesStatic(userId)
    }
    fun getUserData(userId: Int): LiveData<User> {
        return userRep.getUser(userId)
    }

    fun getRoleNameById(roleId: Int): LiveData<String> {
        return roleRep.getRoleNameById(roleId)
    }
    fun getUserDocumentDependenceRoom(userId: Int): LiveData<List<DocDependenceFullInfo>> {
        return dependenceRep.getDependenceInfoByUserId(userId)
    }
    fun getUserDocumentDependenceServ(userId: Int): LiveData<String>{
        return dependenceRep.getDocDependenceUserId(userId)
    }
//    fun getDependenceLiveInfo(): LiveData<String>{
//        return dependenceRep.getDependenceLiveInfo()/
//    }
//    fun getExperienceLiveInfo(): LiveData<String>{
//        return experienceRep.getExperienceLiveInfo()
//    }
    fun getUserDocumentDependenceByIdsRoom(userId: Int, iDs: List<Int>, mod: String): LiveData<List<DocDependenceFullInfo>> {
        return dependenceRep.getDependenceInfoFilterIdListByUserId(userId, iDs, mod)
    }
    fun getUserExperienceRoom(userId: Int): LiveData<List<ExperienceInfo>> {
        return experienceRep.getExperienceFullInfoByUserId(userId)
    }
    fun getUserExperienceServ(curUserId: Int, userId: Int): LiveData<String> {
        return experienceRep.getExperienceInfoByUserIdFromServer(curUserId, userId)
    }
    fun getUserAppExpServ(curUserId: Int, userId: Int): LiveData<String> {
        return experienceRep.getAppExpInfoByUserIdFromServer(token, curUserId, userId)
    }
    fun postUserAppExpServ( curUserId: Int, userId: Int, addFile: MultipartBody.Part,
                            name: RequestBody, type: RequestBody,
                            reason: RequestBody, points: RequestBody,
                            status: RequestBody): LiveData<String> {

        return experienceRep.postAppExpInfoByUserIdToServer(token, curUserId, userId, addFile, name, type, reason, points, status)

    }
    fun putUserAppExpServ( curUserId: Int, userId: Int, expId: Int, addFile: MultipartBody.Part,
                            name: RequestBody, type: RequestBody,
                            reason: RequestBody, points: RequestBody,
                            status: RequestBody): LiveData<String> {

        return experienceRep.putAppExpInfoByUserIdToServer(token, curUserId, userId, expId, addFile, name, type, reason, points, status)

    }
    fun putUserServ(curUserId: Int, userId: Int, user: UpdateUser): LiveData<String> {

        return userRep.putUserToServ(token, curUserId, userId, user)

    }

    fun getUserAppExpRoom(userId: Int): LiveData<List<UserExperience>> {
        return experienceRep.getAppExpInfoByUserIdFromRoom(userId)
    }

    fun getUserExperienceByIdsRoom(userId: Int, iDs: List<Int>, mod: String): LiveData<List<ExperienceInfo>> {
        return experienceRep.getExperienceFullInfoFilterIdListByUserId(userId, iDs, mod)
    }

    fun getSpecializationInfoFromServ(){
        return specToEduRep.getSpecializationInfoFromServ()
    }

    fun getUserDocsInfoFromServ(userId: Int, mod: String): LiveData<String>{
        return dependenceRep.getUserDocsFromServ(token,userId, mod)
    }

    fun postDocument(document: AddDocumentFullInfo, userId: Int, mod: String): LiveData<String> {
        return userRep.postDocumentToServ(token, document, userId, mod)
    }
//    fun postExpFile(addFile: MultipartBody.Part, name: RequestBody, date1: RequestBody, date2: RequestBody, studId: Int): AddAttendancesResponse{
//        return experienceRep.postStudFileToServ(token, name, date1,date2, addFile, studId)
//    }

    fun postExpFile(addFile: MultipartBody.Part, authUserId: Int, userId: Int, name: RequestBody, role: RequestBody, place: RequestBody, expTimeId: RequestBody, experience: RequestBody): LiveData<String>{
        return experienceRep.postExpFileToServ(token, authUserId, userId, addFile, name, role, place, expTimeId, experience)
    }


    fun postSpecFile(addFile: MultipartBody.Part, userId: Int, name: RequestBody, specId: RequestBody, eduId: RequestBody): LiveData<String>{
        return specToEduRep.postSpecToEduFileToServ(token, userId, addFile, name, specId, eduId)
    }
    fun getCurArchiveNameById(archId: Int): LiveData<Archive>{
        return  archiveRep.getCurArchiveNameById(archId)
    }
    //Работа с откликами по вакансиям и резюме
    fun getDocResponsesByUserIdFromRoom(userId: Int, docId: Int): LiveData<List<String>> {
        return reponseRep.getDocResponseByUserId(userId, docId)
    }
    fun getDocResponsesByUserIdWithoutNamesFromRoom(userId: Int, docId: Int, types: List<String>): LiveData<List<String>> {
        return reponseRep.getDocResponseByUserIdWithoutNames(userId, docId, types)
    }

    fun postResponse(userId: Int, respText: List<String>, docId: Int): LiveData<String>{
        return reponseRep.postResponseToServ(token, respText, userId, docId)
    }

    fun postResponseWithNoLiveData(userId: Int, respText: List<String>, docId: Int){
        return reponseRep.postResponseToServ2(token, respText, userId, docId)
    }

    fun postArchive(userId: Int, name: String): LiveData<PostResponse>{
        return archiveRep.postArchiveToServ(token, name, userId)
    }
    fun putArchive(userId: Int, name: String, archiveId: Int): LiveData<String>{
        return archiveRep.putArchiveToServ(token, name, userId, archiveId)
    }
    fun deleteArchiveRequest(archId: Int, userId: Int): LiveData<String> {
        val item =  archiveRep.deleteArchiveByIdFromServ(token, archId, userId)
        return item
    }
    fun deleteDependenceRequest(dependId: Int, userId: Int): LiveData<String> {
        val item =  dependenceRep.deleteDocDependenceUserId(token, dependId, userId)
        return item
    }
    fun getFileMimeRequest(fileId: Int, userId: Int): LiveData<String> {
        val item =  dependenceRep.getFileMimeUserId(token, fileId, userId)
        return item
    }
    fun deleteExperienceRequest(expId: Int, userId: Int): LiveData<String> {
        val item =  experienceRep.deleteExperienceUserId(token, expId, userId)
        return item
    }

    fun deleteAppExperienceRequest(authUserId: Int, userId: Int, expId: Int): LiveData<String> {
        return experienceRep.deleteAppExpUserId(token,authUserId, userId, expId)
    }

    fun putExpFile(addFile: MultipartBody.Part, userId: Int, expId: Int, name: RequestBody, role: RequestBody, place: RequestBody, expTimeId: RequestBody, experience: RequestBody): LiveData<String>{
        return experienceRep.putExpFileToServ(token, userId, expId, addFile, name, role, place, expTimeId, experience)
    }

    fun putDependFile(addFile: MultipartBody.Part, userId: Int, dependId: Int, name: RequestBody, specId: RequestBody, eduId: RequestBody): LiveData<String>{
        return specToEduRep.putSpecToEduFileToServ(token, userId, dependId, addFile, name, specId, eduId)
    }
    
    fun deleteDocRequest(docId: Int, userId: Int): LiveData<String> {
        val item =  userRep.deleteDocByIdFromServ(docId, userId, token)
        return item
    }
    fun deleteDocDepRequest(respId: Int, userId: Int): LiveData<String> {
        val item =  userRep.deleteDocRespByIdFromServ(respId, userId, token)
        return item
    }
//    fun deleteDocRequest(docId: Int, userId: Int): String{
//        var result = ""
//        userRep.deleteDocByIdFromServ(docId, userId, token).observe(getApplication()){
//            result = it.body() as String
//            println("в моделе $result")
//        }
//        return result
//    }

}