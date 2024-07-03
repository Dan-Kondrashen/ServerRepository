package ru.kondrashen.diplomappv20.presentation.activitys

import android.content.Context

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.intl.Locale
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ActivityMainBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.ChangeAppLanguageHelper
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.fragments.MainFragmentEmployee

class MainPageActivity: AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val dataModel: MainPageViewModel by viewModels()
    private val dataModel2: ExtraInfoPageViewModel by viewModels()
    private val userAccountViewModel: UserAccountControlViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    companion object {
        const val TAG = "MainActivity"
        fun newIntent(context: Context?, id: Int, type: String): Intent {
            val intent = Intent(context, MainPageActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("type", type)
            return intent
        }
        fun newIntentGuest(context: Context?, type: String): Intent {
            val intent = Intent(context, MainPageActivity::class.java)
            intent.putExtra("type", type)
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = application.getSharedPreferences("AuthPref", Context.MODE_PRIVATE)
        val editor = pref.edit()
        val prefTheme = application.getSharedPreferences("ThemePref", Context.MODE_PRIVATE)
        val themeInUse = prefTheme.getString(PublicConstants.THEME_SWITCH_KEY, null)
        var language = prefTheme.getString(PublicConstants.SELECTED_LANGUAGE, "en")

        themeInUse?.let {
            this.setTheme(resources.getIdentifier(themeInUse, "style", this.packageName))
        }
        language?.let {
            ChangeAppLanguageHelper().setLocale(this , language)
            var curLoc = this.resources.configuration.locales.get(0)
            var locale = Locale(language)
            if (curLoc.language != locale.language)
                recreate()

        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setIcon(R.mipmap.ic_launcher)
        val id = intent.getIntExtra("id", 0)
        println(id.toString())
        val type = intent.getStringExtra("type")
        editor.putString(PublicConstants.USER_ID, id.toString())
        editor.apply()
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val bundle = Bundle()
        println(id.toString() + "fnajsdndf")
        bundle.putInt("userId", id)
        bundle.putString("userType", type)
        when (type) {
            in listOf("соискатель") -> {
                navController.setGraph(R.navigation.nav_host_fragment_main_reg_employee, bundle)
            }
            "работодатель" -> navController.setGraph(R.navigation.nav_host_fragment_main_reg_employer, bundle)
            "администратор" -> {
                navController.setGraph(R.navigation.nav_host_fragment_main_admin, bundle)
            }
            else -> navController.setGraph(R.navigation.nav_host_fragment_main_unreg)
        }
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            dataModel.saveFBTokenToServ(id, token)
            // Log and toast
            val msg = getString(R.string.msg_token_fmt) + token
            Log.i(TAG, msg)
//            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })
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
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}