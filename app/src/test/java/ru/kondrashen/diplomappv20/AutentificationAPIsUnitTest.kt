package ru.kondrashen.diplomappv20

import android.app.Application
import org.junit.Test
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.kondrashen.diplomappv20.domain.AuthViewModel
import ru.kondrashen.diplomappv20.presentation.fragments.LoginFragment
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddUser
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.UserLog

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(RobolectricTestRunner::class)
class AutentificationAPIsUnitTest {
    private val application: Context = ApplicationProvider.getApplicationContext()
    private val viewModel = AuthViewModel(application as Application)

//    @Mock
//    private lateinit var context: Context

    private lateinit var logFrag: LoginFragment

//    @Before
//    fun setup() {
//        // Инициализация моков
//        MockitoAnnotations.openMocks(this)
//        dataModel = mock(AuthViewModel::class.java)
//
//        val mockContext = mock(Context::class.java)
//        val mockActivity = mock(FragmentActivity::class.java)
//        // Внедрение моков в фрагмент
//        logFrag = LoginFragment(mockContext)
//    }

    @Test
    fun login_isCorrect() {
        val resp = viewModel.login(UserLog("dankonad@yandex.com", "123", 1))
        assertEquals("Вы успешно вошли в систему!", resp.status)
    }

    @Test
    fun registration_isCorrect() {
        val resp = viewModel.register(AddUser(fname = "Dan", lname = "Konad", mname = "",
            phone = 88008008080, email = "dankonad@yandex.com", "123", 1))
        assertEquals("Пользователь успешно добавлен!", resp.status)
    }

}