package ru.kondrashen.diplomappv20.presentation.activitys

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment


import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.google.firebase.FirebaseApp
import ru.kondrashen.diplomappv20.databinding.RegOrLogActivityBinding
import ru.kondrashen.diplomappv20.domain.AuthViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ChangeAppLanguageHelper
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.fragments.LoginFragment
import ru.kondrashen.diplomappv20.presentation.fragments.RegistrationFragment


class AuthActivity: FragmentActivity() {
    private lateinit var binding: RegOrLogActivityBinding
    private val dataModel: AuthViewModel by viewModels()
    companion object{
        fun newIntent(packageContext: Context?): Intent? {
            val intent = Intent(packageContext, AuthActivity::class.java)
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = application.getSharedPreferences("ThemePref", Context.MODE_PRIVATE)
        var themeInUse = pref.getString(PublicConstants.THEME_SWITCH_KEY, null)
        var language = pref.getString(PublicConstants.SELECTED_LANGUAGE, null)
        language?.let {
            ChangeAppLanguageHelper().setLocale(this, language)
        }
        themeInUse?.let {
            this.setTheme(resources.getIdentifier(themeInUse, "style", this.packageName))
        }
        super.onCreate(savedInstanceState)
        binding = RegOrLogActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.extras
//        FirebaseApp.initializeApp(application)
        val pagerAdapter = AuthenticationPagerAdapter(
            supportFragmentManager, lifecycle
        )

        pagerAdapter.addFragment(LoginFragment())
        pagerAdapter.addFragment(RegistrationFragment())
        binding.viewPager.adapter = pagerAdapter
    }

    internal class AuthenticationPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fm, lifecycle) {
        private val fragmentList: MutableList<Fragment> = mutableListOf()

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }

        override fun getItemCount(): Int {
            return fragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]
        }
    }
    fun makeAnim(startAnim: Int, endAnim: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                startAnim, endAnim
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(
                startAnim,
                endAnim
            )
        }
    }
}