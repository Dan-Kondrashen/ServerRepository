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
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.ExperienceTime
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.Views
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.db.WorkSearcherDB
import java.util.concurrent.TimeUnit


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@Config(manifest = "C:\\Users\\user\\AndroidStudioProjects\\DiplomAppV20\\app\\src\\main\\AndroidManifest.xml")
class MainDocumentInfoDatabaseUnitTest {
    @JvmField @Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: WorkSearcherDB
    private lateinit var userDao: UserDAO
    private lateinit var documentDAO: DocumentDAO
    private lateinit var responseDAO: ResponseDAO
    private lateinit var knowDAO: KnowledgeDAO
    private lateinit var viewDAO: ViewsDAO
    private var testId = 0
    private val user = User(id = 100, lname = "Иванов", fname = "Иван", mname = null,
        email = "ivanov@email.com", phone = 88008008080,
        status = "success", roleId = 1,  registration_date = "2024-05-10")
    private val user2 = User(id = 101, lname = "ООО 'Рассвет'", fname = "Иванов Иван Иванович", mname = null,
        email = "ivanov@email.com", phone = 88008008080,
        status = "success", roleId = 1,  registration_date = "2024-05-10")

    @Before
    fun createDb() {
        // Создание In-Memory Database
        db = Room.inMemoryDatabaseBuilder(context, WorkSearcherDB::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.authDao()
        documentDAO = db.docDao()
        viewDAO = db.docViewDao()
        knowDAO = db.knowDao()
        responseDAO = db.docResponseDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun getDocumentsLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            var docList = mutableListOf<Document>()
            while (testId < 10) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "vacancy"
                )
                docList.add(document)
                println(document.docId.toString() + " " + document.type + " " )
                documentDAO.addItem(document)
            }
            while (testId in 10..19) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "resume"
                )
                docList.add(document)
                documentDAO.addItem(document)
            }
//            documentDAO.addItems(*docList.toTypedArray())
        }
        val docAdded = documentDAO.getNewDocsWithKnowMainInfo("vacancy")
        docAdded.test()
            .awaitValue(15, TimeUnit.SECONDS)
            .assertValue { it.size == 10}
    }

    @Test
    fun getMostViewedDocumentsLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            while (testId < 10) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "vacancy"
                )

                documentDAO.addItem(document)
                if (testId <=5){
                    val view = Views(testId, testId, "view", (testId))
                    viewDAO.addItem(view)
                }
            }
            while (testId in 10..19) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "resume"
                )

                documentDAO.addItem(document)
                if (testId <=15){
                    val view = Views(testId, testId, "view", (testId -10))
                    viewDAO.addItem(view)
                }
            }
        }
        val docAdded = documentDAO.getMostViewedDocsWithKnowMainInfo("vacancy")
        docAdded.test()
            .awaitValue(15, TimeUnit.SECONDS)
            .assertValue { it.size == 5}
    }

    @Test
    fun getDocumentsForUserKnowledgeLocal_isSuccess() {
        runTest{
            val knowledge = Knowledge(knowId = 1, name = "SQL", description = null)
            knowDAO.addItem(knowledge)
            userDao.addItem(user)
            while (testId < 10) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "vacancy"
                )
                documentDAO.addItem(document)

                if (testId <=5){
                    knowDAO.addKnowToDoc(KnowledgeToDocumentCrossRef(testId, 1))
                }
            }
            while (testId in 10..19) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "resume"
                )

                documentDAO.addItem(document)
                if (testId <=15){
                    knowDAO.addKnowToDoc(KnowledgeToDocumentCrossRef(testId, 1))
                }
            }
        }
        val docAdded = documentDAO.getUserKnowledgeDocsWithKnowMainInfo("vacancy", listOf(1))
        docAdded.test()
            .awaitValue(10, TimeUnit.SECONDS)
            .assertValue { it.size == 5}
    }

    @Test
    fun getRespDocumentsLocal_isSuccess() {
        runTest{
            val user = User(id = 100, lname = "Иванов", fname = "Иван", mname = null,
                email = "ivanov@email.com", phone = 88008008080,
                status = "success", roleId = 1,  registration_date = "2024-05-10")
            userDao.addItem(user)
            while (testId < 3) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "vacancy"
                )

                documentDAO.addItem(document)
                if (testId <=3){
                    val response = DocResponse(id = testId, type ="view", userId = 100, docId = testId, status = "new")
                    responseDAO.addItem(response)
                }
            }
            while (testId in 3..10) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10",
                    type = "vacancy"
                )
                documentDAO.addItem(document)
            }
        }
        val docAdded = documentDAO.getUserRespDocsWithMoreInfo("view")
        docAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.size == 3}
    }

    @Test
    fun getUserDocumentsLocal_isSuccess() {
        runTest{
            userDao.addItem(user)
            userDao.addItem(user2)


            while (testId < 3) {
                testId +=1
                val document = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 100, date = "2024-05-10", type = "vacancy"
                )
                val document2 = Document(
                    docId = testId, title = "ключ теста 1",
                    salaryF = null, salaryS = null,
                    extra_info = "", contactinfo = "",
                    userId = 101, date = "2024-05-10",
                    type = "vacancy"
                )
                documentDAO.addItem(document)
                documentDAO.addItem(document2)
            }
        }
        val docAdded = documentDAO.getUserDocumentsByUserId(101)
        docAdded.test()
            .awaitValue(5, TimeUnit.SECONDS)
            .assertValue { it.size == 3}
    }

}