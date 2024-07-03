package ru.kondrashen.diplomappv20.presentation.fragments
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.intl.Locale
import androidx.fragment.app.Fragment
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.AnalysisPagerFragmentBinding
import ru.kondrashen.diplomappv20.databinding.PreferenceFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.activitys.MainPageActivity
import ru.kondrashen.diplomappv20.presentation.adapters.AnalysisPagerAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.ThemeListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.ChangeAppLanguageHelper
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants


class AppPreferenceFragment : Fragment() {
    private var _binding: PreferenceFragmentBinding? = null
    private var adapter: ThemeListAdapter? = null
    private var isThemeOpen: Boolean = true
    private val binding get() = _binding!!
    private var languageTag: String? =null

    companion object {
        private const val TAG = "PreferencesFragment"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = PreferenceFragmentBinding.inflate(inflater, container, false)
        updateUI()
        return binding.root
    }


    private fun updateUI() {
        if (isAdded && activity != null) {
            adapter = ThemeListAdapter(activity as AppCompatActivity)
            binding.themeRecycle.adapter = adapter
            val languageArray = requireContext().resources.getStringArray(R.array.pref_languages)
            val languageIndexArray = requireContext().resources.getStringArray(R.array.pref_language_short)
            binding.languageSelector.apply {
                adapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, languageArray)
                onItemSelectedListener = object : OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        languageTag = languageIndexArray[position]

                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }
            }
            binding.themeBtn.setOnClickListener {
                isThemeOpen = !isThemeOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (!isThemeOpen) {
                    it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    binding.themeRecycle.visibility = View.VISIBLE
                } else {
                    binding.themeRecycle.visibility = View.GONE
                    it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                }
            }
            binding.applySettings.setOnClickListener {
                requireActivity().application.getSharedPreferences("ThemePref", Context.MODE_PRIVATE)
                    .edit().putString(PublicConstants.SELECTED_LANGUAGE, languageTag)
                    .apply()
                languageTag?.let {
                    ChangeAppLanguageHelper().setLocale(requireActivity(), it)
                    requireActivity().recreate()
                }
            }

        }
    }

}