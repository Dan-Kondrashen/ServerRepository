package ru.kondrashen.diplomappv20.repository.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.CommentsDAO
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfo
import ru.kondrashen.diplomappv20.repository.dao.DependenciesDAO
import ru.kondrashen.diplomappv20.repository.dao.DocumentDAO
import ru.kondrashen.diplomappv20.repository.dao.EducationDAO
import ru.kondrashen.diplomappv20.repository.dao.ExperienceDAO
import ru.kondrashen.diplomappv20.repository.dao.ExperienceTimeDAO
import ru.kondrashen.diplomappv20.repository.dao.KnowledgeDAO
import ru.kondrashen.diplomappv20.repository.dao.ResponseDAO
import ru.kondrashen.diplomappv20.repository.dao.SpecializationDAO
import ru.kondrashen.diplomappv20.repository.dao.UserDAO
import ru.kondrashen.diplomappv20.repository.dao.ViewsDAO
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.Views
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ChatTitleAndAnotherUserNick
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocCounts
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentPreference
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ResponseInfo
import ru.kondrashen.diplomappv20.repository.data_class.relationship.DependenciesToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.ExperienceToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToUserCrossRef
import java.net.ConnectException
import java.util.Date
import kotlin.coroutines.CoroutineContext

class DocumentsRepository(private val docDAO: DocumentDAO, private val userDAO: UserDAO,
                          private val specDAO: SpecializationDAO, private val eduDAO: EducationDAO,
                          private val knowDAO: KnowledgeDAO, private val docDependDAO: DependenciesDAO,
                          private val docResponseDAO: ResponseDAO, private val docViewsDAO: ViewsDAO,
                          private val expDAO: ExperienceDAO, private val expTimeDAO: ExperienceTimeDAO,
                          private val commentsDAO: CommentsDAO): CoroutineScope {
    private val documentsAPI = APIFactory.docApi
    private val experienceAPI = APIFactory.expApi
    private lateinit var documents: LiveData<List<Document>>
    private lateinit var stringResp: MutableLiveData<String>
    private lateinit var documentsStartInfo: LiveData<List<DocumentInfo>>
//    private var documents: LiveData<List<Document>> =
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job
    fun getDocsInfoByType(type: String, mod: String): LiveData<List<DocumentInfo>>{
        if (type in listOf("resume", "vacancy")){
            return when(mod){
                "new" -> docDAO.getNewDocsMainInfo(type)
                "regim" -> docDAO.getUserTimeDocsMainInfo(type)
                "mostviewed" -> docDAO.getMostViewedDocsMainInfo(type)
                else -> docDAO.getCustomMainInfo(type)
            }
        }
        return when(type){
            in listOf("resume", "vacancy" )-> docDAO.getCustomMainInfo(type)
            "new"-> docDAO.getCustomMainInfo("type")
            else -> docDAO.getCustomMainInfo(type)
        }
    }
    fun getDocsInfoByTypeFull(type: String, mod: String): LiveData<List<DocumentInfoWithKnowledge>>{
        if (type in listOf("resume", "vacancy")){
            return when(mod){
                "new" -> docDAO.getNewDocsWithKnowMainInfo(type)
                "regim" -> docDAO.getNewDocsWithKnowMainInfo(type)
                "mostviewed" -> docDAO.getMostViewedDocsWithKnowMainInfo(type)
                else -> docDAO.getNewDocsWithKnowMainInfo(type)
            }
        }
        return  docDAO.getNewDocsWithKnowMainInfo(type)
    }
    fun getDocsInfoFiltered(type: String, mod: String, documentPreference: DocumentPreference): LiveData<List<DocumentInfoWithKnowledge>>{
        if (type in listOf("resume", "vacancy")){
            return when(mod){
                "asc" -> {
                    when(documentPreference.orderType){
                        "date" -> {
                            if (documentPreference.numItems != null)
                                docDAO.getFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 100)
                        }
                        "number" -> {
                            if (documentPreference.numItems != null)
                                docDAO.getNumViewsFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getNumViewsFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 100)
                        }
                        "wage" -> {
                            if (documentPreference.numItems != null)
                                docDAO.getSalaryFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getSalaryFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 100)
                        }
                        else -> {
                            if (documentPreference.numItems != null)
                                docDAO.getFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getFilteredDocsWithKnowMainInfoAsc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 100)
                        }
                    }
                }
                "desc" -> {
                    when(documentPreference.orderType){
                        "date" -> {
                            if (documentPreference.numItems != null)
                                docDAO.getFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 1000000)
                        }
                        "number" -> {
                            if (documentPreference.numItems != null)
                                docDAO.getNumViewsFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getNumViewsFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 1000000)
                        }
                        "wage" -> {
                            if (documentPreference.numItems != null)
                                docDAO.getSalaryFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getSalaryFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 1000000)
                        }
                        else -> {
                            if (documentPreference.numItems != null)
                                docDAO.getFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, documentPreference.numItems)
                            else
                                docDAO.getFilteredDocsWithKnowMainInfoDesc(documentPreference.dateEnd, documentPreference.dateStart, documentPreference.salaryF,  documentPreference.salaryS, type, documentPreference.knowIdList, 1000000)
                        }
                    }
                }
                else -> docDAO.getNewDocsWithKnowMainInfo(type)
            }
        }
        return  docDAO.getNewDocsWithKnowMainInfo(type)
    }
    fun getDocsInfoByUserRespType(type: String): LiveData<List<DocumentInfoWithKnowledge>>{
        return  docDAO.getUserRespDocsWithMoreInfo(type)
    }
    fun getDocsInfoByUserId(userId: Int): LiveData<List<DocumentInfoWithKnowledge>>{
        return  docDAO.getUserDocumentsByUserId(userId)
    }

//    fun getDocsInfoByTypeKnow(type: String): LiveData<List<DocumentInfoForMainView>>{
//        return  docDAO.getNewDocsWithMainInfo(type)
//    }

    fun getDocumentById(id: Int): LiveData<DocumentInfoWithKnowledge>{
        return docDAO.getDocumentById(id)
    }
    fun getTitleAndUserNick(docId: Int, userId: Int): LiveData<ChatTitleAndAnotherUserNick>{
        return docDAO.getTitleAndUserNick(docId, userId)
    }

    // Получение откликов для выбора чата

    fun getUserRespInfo(userId: Int): LiveData<List<ResponseInfo>>{
        return  docDAO.getUserRespInfo(userId)
    }

    // Получение откликов для выбора чата с сервера

    fun getUserRespFromServ(userId: Int, num: Int): LiveData<String> {
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try {
                val resp = documentsAPI.getUserRespAsync(userId, num)
                if (resp.isSuccessful) {

                    val jsonObject = resp.body()
                    jsonObject?.let {
                        val responseDocJson = it.getAsJsonArray("response")
                        val commentsJson = it.getAsJsonArray("comments")
                        for (i in 0..<responseDocJson.size()) {
                            var jsonLocObj = responseDocJson[i].asJsonObject
                            val userJson = jsonLocObj.getAsJsonObject("user")
                            val documentJson = jsonLocObj.getAsJsonObject("document")
                            val userIdLoc = userJson.get("id").asInt
                            if (userIdLoc != userId) {
                                val user = User(
                                    userJson.get("id").asInt,
                                    userJson.get("fname").asString,
                                    userJson.get("lname").asString,
                                    if (userJson.get("mname").isJsonNull) null else userJson.get("mname").asString,
                                    if (userJson.get("status").isJsonNull) null else userJson.get("status").asString,
                                    null,
                                    null,
                                    userJson.get("roleId").asInt,
                                    Date().toString()
                                )
                                userDAO.addItem(user)
                                println(user.toString() +"вот так")
                            }
                            var document = Document(
                                documentJson.get("id").asInt,
                                documentJson.get("title").asString,
                                if (documentJson.get("salaryF").isJsonNull) 0F else documentJson.get("salaryF").asFloat,
                                if (documentJson.get("salaryS").isJsonNull) 0F else documentJson.get("salaryS").asFloat,
                                documentJson.get("extra_info").toString(),
                                documentJson.get("contactinfo").toString(),
                                documentJson.get("type").asString,
                                documentJson.get("userId").asInt,
                                documentJson.get("date").toString(),
                            )
                            docDAO.addDocument(document)
                            val response = DocResponse(
                                docId = jsonLocObj.get("docId").asInt,
                                status = jsonLocObj.get("statys").asString,
                                id = jsonLocObj.get("id").asInt,
                                userId = jsonLocObj.get("userId").asInt,
                                type = jsonLocObj.get("type").asString,
                            )
                            docResponseDAO.addItem(response)
                        }
                        if (commentsJson.size() != 0) {
                            for (j in 0..<commentsJson.size()) {
                                val item = commentsJson[j].asJsonObject
                                val comm = Comment(
                                    respId = item.get("respId").asInt,
                                    comment_date = item.get("comment_date").asString,
                                    content = item.get("content").asString,
                                    id = item.get("id").asInt,
                                    status = item.get("status").asString,
                                    userId = item.get("userId").asInt,
                                )
                                commentsDAO.addItem(comm)
                            }
                        }
                    }
                    stringResp.postValue("OK")
                }
                stringResp.postValue("No connection")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("No connection")
            }
        }
        return stringResp
    }

    fun getRespDocsFromServ(type: String, userId: Int): LiveData<String>{
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try {
                val resp = documentsAPI.getUserRespDocumentsAsync(userId, type)
                if (resp.isSuccessful) {
                    val jsonObject = resp.body()
                    docResponseDAO.deleteByRespType(type)
                    stringResp.postValue("success")
                    var docList = mutableListOf<Document>()
                    for (i in jsonObject!!) {
                        println(i)
                        val docId = i.get("id").asInt
                        val doc = Document(
                            docId,
                            i.get("title").asString,
                            if (i.get("salaryF").isJsonNull || i.get("salaryF").asString == "" ) 0F else i.get("salaryF").asFloat,
                            if (i.get("salaryS").isJsonNull || i.get("salaryS").asString == "" ) 0F else i.get("salaryS").asFloat,
                            i.get("extra_info").toString(),
                            i.get("contactinfo").toString(),
                            i.get("type").asString,
                            i.get("userId").asInt,
                            i.get("date").toString(),
                            )
                        docList.add(doc)

                        val userJson = i.getAsJsonObject("user")
                        val specEduDocUser = i.getAsJsonArray("spec_to_edu_to_user")
                        val knowDocJson = i.getAsJsonArray("knowledge")
                        val expDocJson = i.getAsJsonArray("experience")
                        val respDocJson = i.getAsJsonArray("response")
                        val viewsDocJson = i.getAsJsonArray("views")
//                        println(respDocJson)

                            val user = User(
                                userJson.get("id").asInt,
                                userJson.get("fname").asString,
                                userJson.get("lname").asString,
                                if (userJson.get("mname").isJsonNull) null else userJson.get("mname").asString,
                                if (userJson.get("status").isJsonNull) null else userJson.get("status").asString,
                                null,
                                null,
                                userJson.get("roleId").asInt,
                                Date().toString()
                            )


                            if (knowDocJson.size() != 0){
                                for (j in 0..< knowDocJson.size()) {
                                    val item = knowDocJson.get(j).asJsonObject
                                    item.let {

                                        val knowledge = Knowledge(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            null
                                        )
                                        knowDAO.addItem(knowledge)
                                        knowDAO.addKnowToDoc(KnowledgeToDocumentCrossRef(docId, knowledge.knowId))
                                    }
                                }
                            }

                            if (viewsDocJson.size() != 0){
                                for (j in 0..<viewsDocJson.size()) {
                                    val item = viewsDocJson[j].asJsonObject

                                    val view = Views(
                                        docId = item.get("docId").asInt,
                                        numviews = item.get("numUsages").asInt,
                                        typeS = item.get("type").asString,
                                        id = null
                                    )
                                    docViewsDAO.deleteCopy(view.docId, view.typeS)
                                    docViewsDAO.addItem(view)
                                }
                            }

                            if (respDocJson.size() != 0){
                                for (j in 0..<respDocJson.size()) {
                                    val item = respDocJson[j].asJsonObject
                                    val response = DocResponse(
                                        docId = item.get("docId").asInt,
                                        status = item.get("statys").asString,
                                        id = item.get("id").asInt,
                                        userId = userId,
                                        type = item.get("type").asString,
                                    )
                                    docResponseDAO.addItem(response)
                                }
                            }

                            if (expDocJson.size() != 0){
                                for (j in 0..< expDocJson.size()) {
                                    val item = expDocJson.get(j).asJsonObject
                                    item.let{
                                        val experience = Experience(
                                            it.get("id").asInt,
                                            it.get("expTimeId").asInt,
                                            if (it.get("role").isJsonNull) null else it.get("role").asString,
                                            if (it.get("experience").isJsonNull) null else it.get("experience").asString,
                                            if (it.get("place").isJsonNull) null else it.get("place").asString,
                                            user.id,
                                            null
                                        )
                                        expDAO.addItem(experience)
                                        expDAO.addExpToDoc(ExperienceToDocumentCrossRef(docId, experience.expId))
                                    }
                                }
                            }

                            if (specEduDocUser.size() != 0) {
                                for (j in 0..< specEduDocUser.size()) {
                                    val item = specEduDocUser.get(j).asJsonObject
                                    val specJson = item.asJsonObject?.getAsJsonObject("specialization")
                                    println(specJson)
                                    val eduJson = if ((item.get("education")).isJsonNull) null else item.getAsJsonObject("education")
                                    println(eduJson)
                                    var specId: Int? = null
                                    var eduId: Int? = null
                                    specJson?.let {
                                        val specialization = Specialization(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            if (it.get("description").isJsonNull) null else it.get("description").asString,
                                        )

                                        specId = specialization.specId
                                        specDAO.addItem(specialization)
                                    }
                                    eduJson?.let {
                                        val education = Education(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            if (it.get("description").isJsonNull) null else it.get("description").asString,
                                        )
                                        eduId = education.id
                                        eduDAO.addItem(education)
                                    }
                                    specId?.let {
                                        docDependDAO.addItem(DocDependencies(
                                            item.get("id").asInt,
                                            documentsScanId = if (item.get("documents_scan_id").isJsonNull) null else item.get("documents_scan_id").asInt,
                                            userId = user.id,
                                            eduId = eduId,
                                            specId = it
                                        ))
                                        specDAO.addSpecToUser(SpecializationToUserCrossRef(
                                            it,
                                            user.id
                                        ))
                                    }
                                }
                            }
                            if (userId != user.id) {
                                userDAO.addItem(user)
                            }
                        }
                    docDAO.addItems(*docList.toTypedArray())
                } else
                    stringResp.postValue("no connection")
            } catch (e: Exception) {
                e.printStackTrace()
                stringResp.postValue("timeout")
            }
        }
        return stringResp
    }
//    fun getRespDocsFromServ(type: String, userId: Int): LiveData<String>{
//        stringResp = MutableLiveData<String>()
//        launch(Dispatchers.IO) {
//            try {
//                val resp = documentsAPI.getUserRespDocumentsAsync(userId, type)
//                if (resp.isSuccessful) {
//                    val jsonObject = resp.body()
//                    docResponseDAO.deleteByRespType(type)
//
//                    for (i in jsonObject!!) {
//                        println(i)
//                        val docId = i.get("id").asInt
//                        val doc = Document(
//                            docId,
//                            i.get("title").asString,
//                            if (i.get("salaryF").isJsonNull) 0F else i.get("salaryF").asFloat,
//                            if (i.get("salaryS").isJsonNull) 0F else i.get("salaryS").asFloat,
//                            i.get("extra_info").toString(),
//                            i.get("contactinfo").toString(),
//                            i.get("type").asString,
//                            i.get("userId").asInt,
//                            i.get("date").toString(),
//                        )
//                        docDAO.deleteItem(doc)
//                        docDAO.addDocument(doc)
//                        val userJson = i.getAsJsonObject("user")
//                        val specEduDocUser = i.getAsJsonArray("spec_to_edu_to_user")
//                        val knowDocJson = i.getAsJsonArray("knowledge")
//                        val expDocJson = i.getAsJsonArray("experience")
//                        val respDocJson = i.getAsJsonArray("response")
//                        val viewsDocJson = i.getAsJsonArray("views")
////                        println(respDocJson)
//
//                        val user = User(
//                            userJson.get("id").asInt,
//                            userJson.get("fname").asString,
//                            userJson.get("lname").asString,
//                            if (userJson.get("mname").isJsonNull) null else userJson.get("mname").asString,
//                            null,
//                            null,
//                            userJson.get("roleId").asInt,
//                            Date().toString()
//                        )
//
//
//                        if (knowDocJson.size() != 0){
//                            for (j in 0..< knowDocJson.size()) {
//                                val item = knowDocJson.get(j).asJsonObject
//                                item.let {
//
//                                    val knowledge = Knowledge(
//                                        it.get("id").asInt,
//                                        it.get("name").asString,
//                                        null
//                                    )
//                                    knowDAO.addItem(knowledge)
//                                    knowDAO.addKnowToDoc(KnowledgeToDocumentCrossRef(docId, knowledge.knowId))
//                                }
//                            }
//                        }
//
//                        if (viewsDocJson.size() != 0){
//                            for (j in 0..<viewsDocJson.size()) {
//                                val item = viewsDocJson[j].asJsonObject
//
//                                val view = Views(
//                                    docId = item.get("docId").asInt,
//                                    numviews = item.get("numUsages").asInt,
//                                    typeS = item.get("type").asString,
//                                    id = null
//                                )
//                                docViewsDAO.deleteCopy(view.docId, view.typeS)
//                                docViewsDAO.addItem(view)
//                            }
//                        }
//
//                        if (respDocJson.size() != 0){
//                            for (j in 0..<respDocJson.size()) {
//                                val item = respDocJson[j].asJsonObject
//                                val response = DocResponse(
//                                    docId = item.get("docId").asInt,
//                                    status = item.get("statys").asString,
//                                    id = item.get("id").asInt,
//                                    userId = user.id,
//                                    type = item.get("type").asString,
//                                )
//                                docResponseDAO.addItem(response)
//                            }
//                        }
//
//                        if (expDocJson.size() != 0){
//                            for (j in 0..< expDocJson.size()) {
//                                val item = expDocJson.get(j).asJsonObject
//                                item.let{
//                                    val experience = Experience(
//                                        it.get("id").asInt,
//                                        it.get("expTimeId").asInt,
//                                        if (it.get("role").isJsonNull) null else it.get("role").asString,
//                                        if (it.get("experience").isJsonNull) null else it.get("experience").asString,
//                                        if (it.get("place").isJsonNull) null else it.get("place").asString,
//                                        user.id,
//                                        null
//                                    )
//                                    expDAO.addItem(experience)
//                                    expDAO.addExpToDoc(ExperienceToDocumentCrossRef(docId, experience.expId))
//                                }
//                            }
//                        }
//
//                        if (specEduDocUser.size() != 0) {
//                            for (j in 0..< specEduDocUser.size()) {
//                                val item = specEduDocUser.get(j).asJsonObject
//                                val specJson = item.asJsonObject?.getAsJsonObject("specialization")
//                                println(specJson)
//                                val eduJson = if ((item.get("education")).isJsonNull) null else item.getAsJsonObject("education")
//                                println(eduJson)
//                                var specId: Int? = null
//                                var eduId: Int? = null
//                                specJson?.let {
//                                    val specialization = Specialization(
//                                        it.get("id").asInt,
//                                        it.get("name").asString,
//                                        if (it.get("description").isJsonNull) null else it.get("description").asString,
//                                    )
//
//                                    specId = specialization.specId
//                                    println("specId")
//                                    println(specId)
//                                    specDAO.addItem(specialization)
//                                }
//                                eduJson?.let {
//                                    val education = Education(
//                                        it.get("id").asInt,
//                                        it.get("name").asString,
//                                        if (it.get("description").isJsonNull) null else it.get("description").asString,
//                                    )
//                                    eduId = education.id
//                                    println("eduId")
//                                    println(eduId)
//                                    eduDAO.addItem(education)
//                                }
//                                specId?.let {
//                                    docDependDAO.addItem(DocDependencies(
//                                        item.get("id").asInt,
//                                        documentsScanId = if (item.get("documents_scan_id").isJsonNull) null else item.get("documents_scan_id").asInt,
//                                        userId = userId,
//                                        eduId = eduId,
//                                        specId = it
//                                    ))
//                                    specDAO.addSpecToUser(SpecializationToUserCrossRef(
//                                        it,
//                                        user.id
//                                    ))
//                                }
//                            }
//                        }
//                        if (userId != user.id) {
//                            userDAO.addItem(user)
//                        }
//                    }
//                    stringResp.postValue("OK")
//                } else
//                    stringResp.postValue("No connection")
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        return stringResp
//    }

    fun getUnRegStartedDataFromServ(type: String, num: Int): String{
        var str =""
        runBlocking(Dispatchers.IO){
            val expInfoList = experienceAPI.getExperienceTimeAsync()
            if (expInfoList.isSuccessful)
                expInfoList.body()?.let { expTimeDAO.addItems(*it.toTypedArray()) }
            val resp = documentsAPI.getNumDocumentsAsync(type, num)
            if (resp.isSuccessful) {
                val jsonObject = resp.body()
                for (i in jsonObject!!){
                    val docId = i.get("id").asInt
                    val doc = Document(
                        docId,
                        i.get("title").asString,
                        if (i.get("salaryF").isJsonNull) 0F else i.get("salaryF").asFloat,
                        if (i.get("salaryS").isJsonNull) 0F else i.get("salaryS").asFloat,
                        i.get("extra_info").toString(),
                        i.get("contactinfo").toString(),
                        i.get("type").asString,
                        i.get("userId").asInt,
                        i.get("date").toString(),
                    )
                    docDAO.addItem(doc)
                    val userJson = i.getAsJsonObject("user")
                    val specEduDocJson = i.getAsJsonArray("spec_to_edu_to_doc")
                    val knowDocJson = i.getAsJsonArray("knowledge")
                    val expDocJson = i.getAsJsonArray("experience")
                    val viewsDocJson = i.getAsJsonArray("views")
                    if (viewsDocJson.size() != 0){
                        val item = viewsDocJson[0].asJsonObject
                        val view = Views(docId = docId, numviews = item.get("numUsages").asInt, id = item.get("id").asInt, typeS = item.get("type").toString())
                        docViewsDAO.deleteCopy(view.docId, view.typeS)
                        docViewsDAO.addItem(view)
                    }
                    val user = User(
                        userJson.get("id").asInt,
                        userJson.get("fname").asString,
                        userJson.get("lname").asString,
                        if (userJson.get("mname").isJsonNull) null else userJson.get("mname").asString,
                        if (userJson.get("status").isJsonNull) null else userJson.get("status").asString,
                        null,
                        null,
                        userJson.get("roleId").asInt,
                        Date().toString()
                    )
                    if (knowDocJson.size() != 0){
                        for (j in 0..< knowDocJson.size()) {
                            val item = knowDocJson.get(j).asJsonObject
                            item.let {
                                val knowledge = Knowledge(
                                    it.get("id").asInt,
                                    it.get("name").asString,
                                     null
                                )
                                knowDAO.addItem(knowledge)
                                knowDAO.addKnowToDoc(KnowledgeToDocumentCrossRef(docId, knowledge.knowId))
                            }
                        }
                    }
                    if (expDocJson.size() != 0){
                        for (j in 0..< expDocJson.size()) {
                            val item = expDocJson.get(j).asJsonObject
                            item.let{
                                val experience = Experience(
                                    it.get("id").asInt,
                                    it.get("expTimeId").asInt,
                                    if (it.get("role").isJsonNull) null else it.get("role").asString,
                                    if (it.get("experience").isJsonNull) null else it.get("experience").asString,
                                    if (it.get("place").isJsonNull) null else it.get("place").asString,
                                    user.id,
                                    null
                                )
                                expDAO.addItem(experience)
                                expDAO.addExpToDoc(ExperienceToDocumentCrossRef(docId, experience.expId))
                            }
                        }
                    }
                    if (specEduDocJson.size() != 0) {
                        for (j in 0..< specEduDocJson.size()) {
                            val item = specEduDocJson.get(j).asJsonObject
                            val specJson = item.asJsonObject?.getAsJsonObject("specialization")
                            println(specJson)
                            val eduJson = if ((item.get("education")).isJsonNull) null else item.getAsJsonObject("education")
                            println(eduJson)
                            var specId: Int? = null
                            var eduId: Int? = null
                            specJson?.let {
                                val specialization = Specialization(
                                    it.get("id").asInt,
                                    it.get("name").asString,
                                    if (it.get("description").isJsonNull) null else it.get("description").asString,
                                )

                                specId = specialization.specId
                                specDAO.addItem(specialization)
                            }
                            eduJson?.let {
                                val education = Education(
                                    it.get("id").asInt,
                                    it.get("name").asString,
                                    if (it.get("description").isJsonNull) null else it.get("description").asString,
                                )
                                eduId = education.id
                                println("eduId")
                                println(eduId)
                                eduDAO.addItem(education)
                            }
                            specId?.let {
                                docDependDAO.addItem(DocDependencies(
                                    item.get("id").asInt,
                                    documentsScanId = if (item.get("documents_scan_id").isJsonNull) null else item.get("documents_scan_id").asInt,
                                    userId = user.id,
                                    eduId = eduId,
                                    specId = it
                                ))
                                specDAO.addSpecToUser(SpecializationToUserCrossRef(
                                    it,
                                    user.id
                                ))
                            }

                        }
                    }

                    userDAO.addItem(user)
                }
                str = "Данные успешно получены!"
            }
            else if (resp.code() == 502) {
                str ="Сервер не доступен! Повторите попытку позже!"
            }
            else{
                str = "ОШИБКА: ${resp.code()} Что-то пошло не так... Попробуйте подкючиться позже!"
            }
        }
        return str
    }
//    fun getExperienceTimeFromServ(): LiveData<String>{
//        var str = MutableLiveData<String>()
//        launch(Dispatchers.IO) {
//            val resp = experienceAPI.getExperienceTimeAsync()
//            if (resp.isSuccessful) {
//                val expInfoList = resp.body()
//                expInfoList?.let { expTimeDAO.addItems(*it.toTypedArray()) }
//            }
//            else{
//                str.postValue("ОШИБКА: ${resp.code()} Что-то пошло не так... Попробуйте подкючиться позже!")
//            }
//        }
//        return str
//    }
    fun getDocumentFilterableServ(userId: Int, type: String, mod: String,
                                  documentPreference: DocumentPreference): LiveData<String>{
        stringResp = MutableLiveData<String>()
        launch(Dispatchers.IO) {
            try {
                val resp = documentsAPI.getDocumentsFilterableAsync(userId, type, mod,
                    documentPreference)
                if (resp.isSuccessful) {
                    val jsonObject = resp.body()
                    val docJson = jsonObject!!.getAsJsonArray("documents")
                    val viewsDocJson = jsonObject.getAsJsonArray("views")
                    val responseDocJson = jsonObject.getAsJsonArray("responses")
                    if (docJson.size() != 0) {
                        for (j in 0..<docJson.size()) {
                            val i = docJson.get(j).asJsonObject
                            val docId = i.get("id").asInt
                            val docUserId = i.get("userId").asInt
                            val doc = Document(
                                docId,
                                i.get("title").asString,
                                if (i.get("salaryF").isJsonNull || i.get("salaryF").asString == "" ) 0F else i.get("salaryF").asFloat,
                                if (i.get("salaryS").isJsonNull|| i.get("salaryS").asString == "" ) 0F else i.get("salaryS").asFloat,
                                if (i.get("extra_info").isJsonNull) "" else i.get("extra_info").asString,
                                if (i.get("contactinfo").isJsonNull) "" else i.get("contactinfo").asString,
                                i.get("type").asString,
                                i.get("userId").asInt,
                                i.get("date").toString(),
                            )
                            docDAO.addItem(doc)
                            val userJson = i.getAsJsonObject("user")
                            val specEduDocUser = i.getAsJsonArray("spec_to_edu_to_user")
                            val knowDocJson = i.getAsJsonArray("knowledge")
                            val expDocJson = i.getAsJsonArray("experience")
                            val user = User(
                                userJson.get("id").asInt,
                                userJson.get("fname").asString,
                                userJson.get("lname").asString,
                                if (userJson.get("mname").isJsonNull) null else userJson.get("mname").asString,
                                if (userJson.get("status").isJsonNull) null else userJson.get("status").asString,
                                null,
                                null,
                                userJson.get("roleId").asInt,
                                Date().toString()
                            )

                            if (knowDocJson.size() != 0) {
                                for (j in 0..<knowDocJson.size()) {
                                    val item = knowDocJson.get(j).asJsonObject
                                    item.let {

                                        val knowledge = Knowledge(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            null
                                        )
                                        knowDAO.addItem(knowledge)
                                        knowDAO.addKnowToDoc(
                                            KnowledgeToDocumentCrossRef(
                                                docId,
                                                knowledge.knowId
                                            )
                                        )
                                    }
                                }
                            }
                            if (expDocJson.size() != 0) {
                                for (j in 0..<expDocJson.size()) {
                                    val item = expDocJson.get(j).asJsonObject
                                    item.let {
                                        val experience = Experience(
                                            it.get("id").asInt,
                                            it.get("expTimeId").asInt,
                                            if (it.get("role").isJsonNull) null else it.get("role").asString,
                                            if (it.get("experience").isJsonNull) null else it.get("experience").asString,
                                            if (it.get("place").isJsonNull) null else it.get("place").asString,
                                            user.id,
                                            null
                                        )
                                        expDAO.addItem(experience)
                                        expDAO.addExpToDoc(
                                            ExperienceToDocumentCrossRef(
                                                docId,
                                                experience.expId
                                            )
                                        )
                                    }
                                }
                            }

                            if (specEduDocUser.size() != 0) {
                                for (j in 0..<specEduDocUser.size()) {
                                    val item = specEduDocUser.get(j).asJsonObject
                                    val specJson =
                                        item.asJsonObject?.getAsJsonObject("specialization")
                                    println(specJson)
                                    val eduJson =
                                        if ((item.get("education")).isJsonNull) null else item.getAsJsonObject(
                                            "education"
                                        )
                                    println(eduJson)
                                    var specId: Int? = null
                                    var eduId: Int? = null
                                    specJson?.let {
                                        val specialization = Specialization(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            if (it.get("description").isJsonNull) null else it.get("description").asString,
                                        )

                                        specId = specialization.specId
                                        println("specId")
                                        println(specId)
                                        specDAO.addItem(specialization)
                                    }
                                    eduJson?.let {
                                        val education = Education(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            if (it.get("description").isJsonNull) null else it.get("description").asString,
                                        )
                                        eduId = education.id
                                        println("eduId")
                                        println(eduId)
                                        eduDAO.addItem(education)
                                    }
                                    specId?.let {
                                        var docDep =  DocDependencies(
                                            item.get("id").asInt,
                                            documentsScanId = if (item.get("documents_scan_id").isJsonNull) null else item.get(
                                                "documents_scan_id"
                                            ).asInt,
                                            userId = docUserId,
                                            eduId = eduId,
                                            specId = it
                                        )
                                        docDependDAO.addItem(docDep)
                                        docDependDAO.addDocDep(DependenciesToDocumentCrossRef(docDep.id, docId))
                                        specDAO.addSpecToUser(
                                            SpecializationToUserCrossRef(
                                                it,
                                                user.id
                                            )
                                        )
                                    }
                                }
                            }
                            userDAO.addItem(user)
                            if (viewsDocJson.size() != 0) {
                                for (j in 0..<viewsDocJson.size()) {
                                    val item = viewsDocJson[j].asJsonObject
                                    val view = Views(
                                        docId = item.get("docId").asInt,
                                        numviews = item.get("numUsages").asInt,
                                        typeS = item.get("type").asString,
                                        id = null
                                    )
                                    docViewsDAO.deleteCopy(view.docId, view.typeS)
                                    docViewsDAO.addItem(view)
                                }
                            }
                            if (responseDocJson.size() != 0) {
                                for (j in 0..<responseDocJson.size()) {
                                    val item = responseDocJson[j].asJsonObject
                                    val response = DocResponse(
                                        docId = item.get("docId").asInt,
                                        status = item.get("statys").asString,
                                        id = item.get("id").asInt,
                                        userId = item.get("userId").asInt,
                                        type = item.get("type").asString,
                                    )
                                    docResponseDAO.addItem(response)
                                }
                            }
                        }
                        stringResp.postValue("success")
                    } else {
                        stringResp.postValue("no items")
                    }
                }
                else if (resp.code() == 502 || resp.code() == 503) {
                    stringResp.postValue("server not available")
                } else {
                    stringResp.postValue("query problem")
                }
            }
            catch (e: ConnectException) {
                e.printStackTrace()
                println("no connection")
                stringResp.postValue("timeout")
            }
            catch (e: Exception){
                e.printStackTrace()
                stringResp.postValue("timeout")
            }
        }

        return stringResp
    }
    fun getRegStartedDataFromServ(userId: Int, type: String, num: Int,startNum: Int, mod: String): LiveData<String>{
        stringResp = MutableLiveData<String>()
        try {
            launch(Dispatchers.IO) {
                val resp = experienceAPI.getExperienceTimeAsync()
                if (resp.isSuccessful) {
                    val expInfoList = resp.body()
                    expInfoList?.let { expTimeDAO.addItems(*it.toTypedArray()) }
                }
            }
            launch(Dispatchers.IO) {
                val resp = documentsAPI.getNumDocumentsForUserAsync(
                    userId,
                    type,
                    mod,
                    DocCounts(num, startNum)
                )
                println(resp)
                if (resp.code() == 200) {
                    val jsonObject = resp.body()
                    val docJson = jsonObject!!.getAsJsonArray("documents")
                    val viewsDocJson = jsonObject.getAsJsonArray("views")
                    val responseDocJson = jsonObject.getAsJsonArray("responses")
                    if (docJson.size() != 0) {
                        for (j in 0..<docJson.size()) {
                            val i = docJson.get(j).asJsonObject
                            val docId = i.get("id").asInt
                            val docUserId = i.get("userId").asInt
                            val doc = Document(
                                docId,
                                i.get("title").asString,
                                if (i.get("salaryF").isJsonNull || i.get("salaryF").asString == "" ) 0F else i.get("salaryF").asFloat,
                                if (i.get("salaryS").isJsonNull|| i.get("salaryS").asString == "" ) 0F else i.get("salaryS").asFloat,
                                if (i.get("extra_info").isJsonNull) "" else i.get("extra_info").asString,
                                if (i.get("contactinfo").isJsonNull) "" else i.get("contactinfo").asString,
                                i.get("type").asString,
                                i.get("userId").asInt,
                                i.get("date").toString(),
                            )
                            docDAO.addItem(doc)
                            val userJson = i.getAsJsonObject("user")
                            val specEduDocUser = i.getAsJsonArray("spec_to_edu_to_user")
                            val knowDocJson = i.getAsJsonArray("knowledge")
                            val expDocJson = i.getAsJsonArray("experience")
                            val user = User(
                                userJson.get("id").asInt,
                                userJson.get("fname").asString,
                                userJson.get("lname").asString,
                                if (userJson.get("mname").isJsonNull) null else userJson.get("mname").asString,
                                if (userJson.get("status").isJsonNull) null else userJson.get("status").asString,
                                null,
                                null,
                                userJson.get("roleId").asInt,
                                Date().toString()
                            )

                            if (knowDocJson.size() != 0) {
                                for (j in 0..<knowDocJson.size()) {
                                    val item = knowDocJson.get(j).asJsonObject
                                    item.let {

                                        val knowledge = Knowledge(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            null
                                        )
                                        knowDAO.addItem(knowledge)
                                        knowDAO.addKnowToDoc(
                                            KnowledgeToDocumentCrossRef(
                                                docId,
                                                knowledge.knowId
                                            )
                                        )
                                    }
                                }
                            }
                            if (expDocJson.size() != 0) {
                                for (j in 0..<expDocJson.size()) {
                                    val item = expDocJson.get(j).asJsonObject
                                    item.let {
                                        val experience = Experience(
                                            it.get("id").asInt,
                                            it.get("expTimeId").asInt,
                                            if (it.get("role").isJsonNull) null else it.get("role").asString,
                                            if (it.get("experience").isJsonNull) null else it.get("experience").asString,
                                            if (it.get("place").isJsonNull) null else it.get("place").asString,
                                            user.id,
                                            null
                                        )
                                        expDAO.addItem(experience)
                                        expDAO.addExpToDoc(
                                            ExperienceToDocumentCrossRef(
                                                docId,
                                                experience.expId
                                            )
                                        )
                                    }
                                }
                            }

                            if (specEduDocUser.size() != 0) {
                                for (j in 0..<specEduDocUser.size()) {
                                    val item = specEduDocUser.get(j).asJsonObject
                                    val specJson =
                                        item.asJsonObject?.getAsJsonObject("specialization")
                                    println(specJson)
                                    val eduJson =
                                        if ((item.get("education")).isJsonNull) null else item.getAsJsonObject(
                                            "education"
                                        )
                                    println(eduJson)
                                    var specId: Int? = null
                                    var eduId: Int? = null
                                    specJson?.let {
                                        val specialization = Specialization(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            if (it.get("description").isJsonNull) null else it.get("description").asString,
                                        )

                                        specId = specialization.specId
                                        println("specId")
                                        println(specId)
                                        specDAO.addItem(specialization)
                                    }
                                    eduJson?.let {
                                        val education = Education(
                                            it.get("id").asInt,
                                            it.get("name").asString,
                                            if (it.get("description").isJsonNull) null else it.get("description").asString,
                                        )
                                        eduId = education.id
                                        println("eduId")
                                        println(eduId)
                                        eduDAO.addItem(education)
                                    }
                                    specId?.let {
                                        var docDep =  DocDependencies(
                                            item.get("id").asInt,
                                            documentsScanId = if (item.get("documents_scan_id").isJsonNull) null else item.get(
                                                "documents_scan_id"
                                            ).asInt,
                                            userId = docUserId,
                                            eduId = eduId,
                                            specId = it
                                        )
                                        docDependDAO.addItem(docDep)
                                        docDependDAO.addDocDep(DependenciesToDocumentCrossRef(docDep.id, docId))
                                        specDAO.addSpecToUser(
                                            SpecializationToUserCrossRef(
                                                it,
                                                user.id
                                            )
                                        )

                                    }

                                }
                            }
                            userDAO.addItem(user)
                            if (viewsDocJson.size() != 0) {
                                for (j in 0..<viewsDocJson.size()) {
                                    val item = viewsDocJson[j].asJsonObject
                                    val view = Views(
                                        docId = item.get("docId").asInt,
                                        numviews = item.get("numUsages").asInt,
                                        typeS = item.get("type").asString,
                                        id = null
                                    )
                                    docViewsDAO.deleteCopy(view.docId, view.typeS)
                                    docViewsDAO.addItem(view)
                                }
                            }
                            if (responseDocJson.size() != 0) {
                                for (j in 0..<responseDocJson.size()) {
                                    val item = responseDocJson[j].asJsonObject
                                    val response = DocResponse(
                                        docId = item.get("docId").asInt,
                                        status = item.get("statys").asString,
                                        id = item.get("id").asInt,
                                        userId = item.get("userId").asInt,
                                        type = item.get("type").asString,
                                    )
                                    docResponseDAO.addItem(response)
                                }
                            }
                        }
                        stringResp.postValue("Данные успешно получены!")
                    } else if (resp.code() == 502) {
                        stringResp.postValue("Сервер не доступен! Повторите попытку позже!")
                    } else {
                        stringResp.postValue("Что-то пошло не так... Попробуйте подкючиться позже!")
                    }
                }
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            stringResp.postValue("no connection to serv")
        }
        return stringResp
    }

}