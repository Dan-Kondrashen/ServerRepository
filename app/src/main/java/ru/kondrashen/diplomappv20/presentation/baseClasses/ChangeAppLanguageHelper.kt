package ru.kondrashen.diplomappv20.presentation.baseClasses

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager
import java.util.Locale


class ChangeAppLanguageHelper {
    fun setLocale(context: Context, language: String): Context? {
        persist(context, language)
        return updateResources(context, language)
    }
    private fun updateResources(context: Context, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration= context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        context.createConfigurationContext(configuration)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        return context.createConfigurationContext(configuration)
    }

    private fun persist(context: Context, language: String) {
        val pref = context.getSharedPreferences("ThemePref", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(PublicConstants.SELECTED_LANGUAGE, language)
        editor.apply()
    }
}