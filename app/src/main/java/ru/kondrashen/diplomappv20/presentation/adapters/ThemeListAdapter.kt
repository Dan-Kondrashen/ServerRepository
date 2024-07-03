package ru.kondrashen.diplomappv20.presentation.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ListItemDocumentAnalysisBinding
import ru.kondrashen.diplomappv20.databinding.ListItemThemeBinding
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentAnalysisInfo
import java.sql.Time
import java.util.Date


class ThemeListAdapter(private var activity: AppCompatActivity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var  themeArray = activity.resources.getStringArray(R.array.pref_app_theme)
    private var  themeNameArray = activity.resources.getStringArray(R.array.pref_app_name_theme)
    private var  themeIdsArray = activity.resources.getStringArray(R.array.pref_theme_ids)
    private var chosenThemeId: Int? = null
    private var resources = activity.resources
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =ListItemThemeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ThemeViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return themeIdsArray.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder1: ThemeViewHolder = holder as ThemeViewHolder
        val name = themeNameArray[position]
        val desc = themeArray[position]
        val id = themeIdsArray[position].toInt()
        holder1.bindAnalysis(name, id, desc)
    }
    inner class ThemeViewHolder(private val binding: ListItemThemeBinding): RecyclerView.ViewHolder(binding.root){
        fun bindAnalysis(name: String, id: Int, desc: String){
            binding.descInfo.text = desc
            binding.themeTitle.text = name
            var drawableId = resources.getIdentifier("theme$id", "drawable", activity.packageName)
            binding.imageTheme.setImageResource(drawableId)
            binding.root.setOnClickListener {
                chosenThemeId = id
                activity.setTheme(resources.getIdentifier("Theme.MyTheme$id", "style", activity.packageName))
                activity.recreate()
                val pref = activity.application.getSharedPreferences("ThemePref", Context.MODE_PRIVATE).edit().putString(PublicConstants.THEME_SWITCH_KEY, "Theme.MyTheme$id").apply()
            }
        }
    }
    fun getChosenTheme(): Int?{
        return chosenThemeId
    }
}