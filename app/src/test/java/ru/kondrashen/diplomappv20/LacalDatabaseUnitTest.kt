package ru.kondrashen.diplomappv20

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jraska.livedata.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import ru.kondrashen.diplomappv20.repository.dao.CommentsDAO
import ru.kondrashen.diplomappv20.repository.dao.DependenciesDAO
import ru.kondrashen.diplomappv20.repository.dao.DocumentDAO
import ru.kondrashen.diplomappv20.repository.dao.EducationDAO
import ru.kondrashen.diplomappv20.repository.dao.ExperienceDAO
import ru.kondrashen.diplomappv20.repository.dao.ExperienceTimeDAO
import ru.kondrashen.diplomappv20.repository.dao.KnowledgeDAO
import ru.kondrashen.diplomappv20.repository.dao.ResponseDAO
import ru.kondrashen.diplomappv20.repository.dao.SkillTypeDAO
import ru.kondrashen.diplomappv20.repository.dao.SpecializationDAO
import ru.kondrashen.diplomappv20.repository.dao.UserDAO
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.ExperienceTime
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.SkillType
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeTypeCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationTypeCrossRef
import ru.kondrashen.diplomappv20.repository.db.WorkSearcherDB
import java.util.concurrent.TimeUnit



@RunWith(AndroidJUnit4::class)
@Config(manifest = "C:\\Users\\user\\AndroidStudioProjects\\DiplomAppV20\\app\\src\\main\\AndroidManifest.xml")
class LacalDatabaseUnitTest {
    @JvmField @Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: WorkSearcherDB
    private lateinit var userDao: UserDAO
    private lateinit var documentDAO: DocumentDAO
    private lateinit var specDAO: SpecializationDAO
    private lateinit var eduDAO: EducationDAO
    private lateinit var skillTypeDAO: SkillTypeDAO
    private lateinit var responseDAO: ResponseDAO
    private lateinit var knowDAO: KnowledgeDAO
    private lateinit var expDAO: ExperienceDAO
    private lateinit var expTimeDAO: ExperienceTimeDAO
    private lateinit var dependenciesDAO: DependenciesDAO
    private lateinit var commentsDAO: CommentsDAO
    private var testId = 0
    private val user = User(id = 100, lname = "Иванов", fname = "Иван", mname = null,
        email = "ivanov@email.com", phone = 88008008080,
        status = "success", roleId = 1,  registration_date = "2024-05-10")
    private val user2 = User(id = 101, lname = "ООО 'Рассвет'", fname = "Иванов Иван Иванович", mname = null,
        email = "ivanov@email.com", phone = 88008008080,
        status = "success", roleId = 1,  registration_date = "2024-05-10")
    private val document = Document(docId = 100, title = "ключ теста 1",
        salaryF = null, salaryS = null,
        extra_info = "", contactinfo = "",
        userId = 100, date = "2024-05-10",
        type = "vacancy")

    @Before
    fun createDb() {
        // Создание In-Memory Database
        db = Room.inMemoryDatabaseBuilder(context, WorkSearcherDB::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.authDao()
        documentDAO = db.docDao()
        specDAO = db.specDao()
        eduDAO = db.eduDao()
        expDAO = db.expDao()
        skillTypeDAO = db.skillTypeDao()
        knowDAO = db.knowDao()
        expTimeDAO = db.expTimeDao()
        dependenciesDAO = db.docDependDao()
        responseDAO = db.docResponseDao()
        commentsDAO = db.commentDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun addUserLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
        }
            val userAdded = userDao.getUser(100)
            userAdded.test()
                .awaitValue(5, TimeUnit.SECONDS)
                .assertValue {it.lname == "Иванов"}
    }

    @Test
    fun deleteUserLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            userDao.deleteUserDataById(userId = 100)
        }
        val userAdded = userDao.getUser(100)
        userAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue {it == null}
    }

    @Test
    fun updateUserLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            userDao.putUser(uId = 100, lname = "Прохаров", fname = "Захар", mname = "Дмитриевич",
                email = "ivanov@email.com", phone = 88008008080)
        }
        val userAdded = userDao.getUser(100)
        userAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue {it.lname == "Прохаров" && it.mname != null}
    }

    @Test
    fun addKnowledgeLocal_isSuccess() {
        runTest{
            val knowledge = Knowledge(knowId = 100, name = "SQL", description = null)
            knowDAO.addItem(knowledge)
        }
        val knowAdded = knowDAO.getKnowledge()
        knowAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue {it.isNotEmpty()}
    }

    @Test
    fun addSpecializationLocal_isSuccess() {
        runTest{
            val specialization = Specialization(specId = 100, name = "", description = null)
            specDAO.addItem(specialization)
        }
        val specAdded = specDAO.getSpecs()
        specAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue {it.isNotEmpty()}
    }

    @Test
    fun addDocumentLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            documentDAO.addItem(document)
        }
        val docAdded = documentDAO.getDocumentById(100)
        docAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue {it.document.title == "ключ теста 1" }
    }

    @Test
    fun deleteDocumentLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            documentDAO.addItem(document)
            documentDAO.deleteDocumentByIdFromRoom(100, 100)
        }
        val docAdded = documentDAO.getDocumentById(100)
        docAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue {it == null}
    }

    @Test
    fun addGraduationLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            val spec = Specialization(specId = 1, name = "Графический дизайнер", description = null)
            specDAO.addItem(spec)
            val edu = Education(id = 1, name = "Тестовый университет", description = null)
            eduDAO.addItem(edu)
            val depend = DocDependencies(id = 1, userId = 100, specId = 1, eduId = 1, documentsScanId = null)
            dependenciesDAO.addItem(depend)
            dependenciesDAO.getDependenciesInfoByUserId(100)

        }
        val dependAdded = dependenciesDAO.getDependenciesInfoByUserId(100)
        dependAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.isNotEmpty() }
    }

    @Test
    fun updateGraduationLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            val spec = Specialization(specId = 1, name = "Графический дизайнер", description = null)
            specDAO.addItem(spec)
            val edu = Education(id = 1, name = "Тестовый университет", description = null)
            eduDAO.addItem(edu)
            val depend = DocDependencies(id = 1, userId = 100, specId = 1, eduId = 1, documentsScanId = null)
            dependenciesDAO.addItem(depend)
            depend.documentsScanId = 1
            dependenciesDAO.updateItem(depend)

        }
        val dependAdded = dependenciesDAO.getDependenceById(1)
        dependAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.docDependence.documentsScanId != null}
    }

    @Test
    fun deleteGraduationLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            val spec = Specialization(specId = 1, name = "Графический дизайнер", description = null)
            specDAO.addItem(spec)
            val edu = Education(id = 1, name = "Тестовый университет", description = null)
            eduDAO.addItem(edu)
            val depend = DocDependencies(id = 1, userId = 100, specId = 1, eduId = 1, documentsScanId = null)
            dependenciesDAO.addItem(depend)
            dependenciesDAO.deleteDepend(1)
        }
        val dependAdded = dependenciesDAO.getDependenceById(100)
        dependAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it == null }
    }

    @Test
    fun addExperienceLocal_isSuccess() {
        var testId = 1
        runTest{
            userDao.addItem(user)
            val expTime =  ExperienceTime(id = 1, experienceTime = "от 1 года до 2 лет")
            expTimeDAO.addItem(expTime)
            val exp = Experience(expId = testId, expTimeId = 1,
                role = null, place = null,  userId = 100,
                documentScanId = null, experience = "" )
            expDAO.addItem(exp)
        }
        val expAdded = expDAO.getExperienceFullInfoByExpId(testId)
        expAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it != null}
    }

    @Test
    fun updateExperienceLocal_isSuccess() {
        var testId = 1
        runTest{
            userDao.addItem(user)
            val expTime =  ExperienceTime(id = 1, experienceTime = "от 1 года до 2 лет")
            expTimeDAO.addItem(expTime)
            val exp = Experience(expId = testId, expTimeId = 1,
                role = null, place = null,  userId = 100,
                documentScanId = null, experience = "" )
            expDAO.addItem(exp)
            val exp2 = Experience(expId = testId, expTimeId = 1,
                role = null, place = "Google",  userId = 100,
                documentScanId = null, experience = "" )
            expDAO.updateItem(exp2)
        }
        val expAdded = expDAO.getExperienceFullInfoByExpId(testId)
        expAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.place != null }
    }

    @Test
    fun deleteExperienceLocal_isSuccess() {
        var testId = 1
        runTest{
            userDao.addItem(user)
            val expTime =  ExperienceTime(id = 1, experienceTime = "от 1 года до 2 лет")
            expTimeDAO.addItem(expTime)
            val exp = Experience(expId = testId, expTimeId = 1,
                role = null, place = null,  userId = 100,
                documentScanId = null, experience = "" )
            expDAO.addItem(exp)
            expDAO.deleteExperience(testId)

        }
        val expAdded = expDAO.getExperienceFullInfoByExpId(testId)
        expAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it == null }
    }

    @Test
    fun addAppExperienceLocal_isSuccess() {
        var testId = 1
        runTest{
            userDao.addItem(user)
            val exp = UserExperience(id = testId, userId = 100, reason = null,
                points = 50, status = "confirmed", type = "increase", documents_scan_id = null)
            expDAO.addAppExpItem(exp)
        }
        val expAdded = expDAO.getAppExpInfoById(testId)
        expAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it != null && it.type in listOf("increase", "decrease") }
    }
    @Test
    fun updateAppExperienceLocal_isSuccess() {
        var testId = 1
        runTest{
            userDao.addItem(user)
            val exp = UserExperience(id = testId, userId = 100, reason = null,
                points = 50, status = "confirmed", type = "increase", documents_scan_id = null)
            expDAO.addAppExpItem(exp)
            val exp2 = UserExperience(id = testId, userId = 100, reason = "test",
                points = 50, status = "confirmed", type = "decrease", documents_scan_id = null)
            expDAO.addAppExpItem(exp2)
        }
        val expAdded = expDAO.getAppExpInfoById(testId)
        expAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it != null && it.type in listOf("increase", "decrease") && it.reason == "test"}
    }
    @Test
    fun deleteAppExperienceLocal_isSuccess() {
        var testId = 1
        runTest{
            userDao.addItem(user)
            val exp = UserExperience(id = testId, userId = 100, reason = null,
                points = 50, status = "confirmed", type = "increase", documents_scan_id = null)
            expDAO.addAppExpItem(exp)
        }
        val expAdded = expDAO.getAppExpInfoById(testId)
        expDAO.deleteAppExp(testId)
        expAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it == null }
    }
    // Получение специальностей по id типа, к которому они относятся
    @Test
    fun getSpecOfTypeLocal_isSuccess() {
        var testId = 1
        runTest{
            val spec = Specialization(specId = testId, name = "Графический дизайнер", description = null)
            specDAO.addItem(spec)
            spec.specId =testId +1
            spec.name = "Верстальщик Figma"
            specDAO.addItem(spec)
            spec.specId = testId +2
            spec.name = "Веб-разработчик на JavaScript"
            specDAO.addItem(spec)
            skillTypeDAO.addItem(SkillType(id = testId, name = "Веб разработка", description = null))
            specDAO.addSpecsTyped(SpecializationTypeCrossRef(testId, testId + 1),
                SpecializationTypeCrossRef(testId, testId + 2))
        }
        val specsByType = specDAO.getSpecSkillNamesById(testId)
        specsByType.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.size ==2 && it.any { obj -> obj.contains("JavaScript")&& it.any { obj -> obj.contains("Figma")}}}
    }
    // Получение навыков по названию типа, к которому они относятся
    @Test
    fun getKnowOfTypeLocal_isSuccess() {
        var testId = 1
        runTest{

            val knowledge = Knowledge(knowId = 100, name = "SQL", description = null)
            knowledge.knowId = testId +1
            knowledge.name = "Kotlin"
            knowDAO.addItem(knowledge)
            knowledge.knowId = testId +2
            knowledge.name = "Java"
            knowDAO.addItem(knowledge)
            knowDAO.addItem(knowledge)
            knowledge.knowId =testId +3
            knowledge.name = "React"
            knowDAO.addItem(knowledge)

            skillTypeDAO.addItem(SkillType(id = testId, name = "Мобильная разработка", description = null))
            knowDAO.addKnowsTyped(KnowledgeTypeCrossRef(testId, testId + 1),
                KnowledgeTypeCrossRef(testId, testId +2)
            )
        }
        val knowledgeByType = knowDAO.getKnowledgeNamesBySkillTypeName("Мобильная разработка")
        knowledgeByType.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.size ==2 && it.any { obj -> obj.contains("Kotlin")&& it.any { obj -> obj.contains("Java")}}}
    }
    @Test
    fun getCommentsByRespLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            testId +=1
            val document = Document(
                docId = testId, title = "ключ теста 1",
                salaryF = null, salaryS = null,
                extra_info = "", contactinfo = "",
                userId = 100, date = "2024-05-10",
                type = "vacancy"
            )
            documentDAO.addItem(document)
            val response = DocResponse(id = testId,
                type ="response", userId = 100,
                docId = testId, status = "new")
            responseDAO.addItem(response)
            commentsDAO.addItem(Comment(id = testId, "test","new", "2012-12-12", 100, testId))
            commentsDAO.addItem(Comment(id = testId +1, "test2","new", "2012-12-12", 100, testId))
        }

        val commentsByResp = commentsDAO.getCommentsWithRespIDs(testId)
        commentsByResp.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.size == 2}
    }
    @Test
    fun updateCommentLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            testId +=1
            val document = Document(
                docId = testId, title = "ключ теста 1",
                salaryF = null, salaryS = null,
                extra_info = "", contactinfo = "",
                userId = 100, date = "2024-05-10",
                type = "vacancy"
            )
            documentDAO.addItem(document)
            val response = DocResponse(id = testId,
                type ="response", userId = 100,
                docId = testId, status = "new")
            responseDAO.addItem(response)
            commentsDAO.addItem(Comment(id = testId, content = "test",status = "new", "2012-12-12", 100, testId))
            commentsDAO.updateCommentById(commId = testId, content = "updateTest", status = "viewed", date = "2012-12-12")
        }

        val comment = commentsDAO.getCommentById(testId)
        comment.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.content == "updateTest"}
    }
    @Test
    fun deleteCommentLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            testId +=1
            val document = Document(
                docId = testId, title = "ключ теста 1",
                salaryF = null, salaryS = null,
                extra_info = "", contactinfo = "",
                userId = 100, date = "2024-05-10",
                type = "vacancy"
            )
            documentDAO.addItem(document)
            val response = DocResponse(id = testId,
                type ="response", userId = 100,
                docId = testId, status = "new")
            responseDAO.addItem(response)
            commentsDAO.addItem(Comment(id = testId, content = "test",status = "new", "2012-12-12", 100, testId))
            commentsDAO.deleteCommentByIdFromRoom(testId)
        }

        val comment = commentsDAO.getCommentById(testId)
        comment.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it == null}
    }
}