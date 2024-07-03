package ru.kondrashen.diplomappv20

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import ru.kondrashen.diplomappv20.presentation.fragments.LoginFragment



@RunWith(AndroidJUnit4::class)
@Config(manifest = "C:\\Users\\user\\AndroidStudioProjects\\DiplomAppV20\\app\\src\\main\\AndroidManifest.xml")
class LoginFragmentUnitTest {
    @Test
    fun logValidation_isSuccess() {
        val logFrag = LoginFragment(null)
        val result = logFrag.logValidator("dankonad@yandex.com", "123")
        assertEquals(true, result)
    }
}