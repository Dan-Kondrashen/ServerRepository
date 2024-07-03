package ru.kondrashen.diplomappv20.presentation.fragments
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ExtrSkillsUserAnalysisGrafFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.DocumentAnalysisListAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentAnalysisInfo


class GraphUserFragment : Fragment() {
    private var userId: Int? = null
    private var flag: Int = 0
    private var userType: String? = null
    private var type: String = ""
    private var mod: String = "all"
    private var labels = mutableListOf<String>()
    private var PieLabels = mutableListOf<String>()
    private lateinit var dataSet: ILineDataSet
    private var dataSetList: MutableList<ILineDataSet> = mutableListOf()
    private var dataViews: MutableList<Entry> = mutableListOf()
    private val item: MutableLiveData<String> = MutableLiveData("")
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private lateinit var userModel: UserAccountControlViewModel
    private lateinit var adapter: DocumentAnalysisListAdapter
    private var isDocumentAnalysisOpen: Boolean = true
    private var isSkillAnalysisOpen: Boolean = true
    private var text: String = ""
    private var _binding: ExtrSkillsUserAnalysisGrafFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "AnalysisFragment-User"
        fun newInstance(userId: Int, userType: String) = GraphUserFragment().apply {
            arguments = Bundle().apply{
                putString(PublicConstants.USER_TYPE, userType)
                putInt(PublicConstants.USER_ID, userId)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (getContext() as AppCompatActivity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        userModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        _binding = ExtrSkillsUserAnalysisGrafFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }
        // Добавление проверки жизненного цикла фрагментов
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            onBackPressedCallback
        )
        updateUI()
    }

    private fun updateUI() {
        userId = arguments?.getInt(PublicConstants.USER_ID)
        if (isAdded) {
            binding.apply {
                userId?.let { locUserId ->
                    extraInfoModel.getUserAnalysisDocFromRoom(locUserId)
                        .observe(viewLifecycleOwner){
                            adapter =
                                DocumentAnalysisListAdapter(it as MutableList<DocumentAnalysisInfo>,
                                requireActivity() as AppCompatActivity)
                            if (flag != 1) {
                                if (it.isEmpty()) {
                                    documentProblems.visibility = VISIBLE
                                } else {
                                    documentProblems.visibility = GONE
                                }
                            }
                            else {
                                documentAnalysis.adapter = adapter
                                documentProblems.visibility = GONE
                            }
                        }
                }

                buttonUserDocsAnalysis.setOnClickListener {
                    flag = 1
                    isDocumentAnalysisOpen = !isDocumentAnalysisOpen
                    TransitionManager.beginDelayedTransition(binding.root)
                    if (!isDocumentAnalysisOpen){
                        progressGetDocs.visibility = VISIBLE
                        documentAnalysis.visibility = VISIBLE

                        userId?.let { uId ->
                            userModel.getUserDocsInfoFromServ(uId, "analysis").observe(viewLifecycleOwner){
                                progressGetDocs.visibility = GONE
                                if (it == "timeout") {
                                    documentProblems.visibility = VISIBLE
                                    documentProblems.text = getString(R.string.server_not_avilable)
                                }
                                else if (it == "No connection") {
                                    documentProblems.visibility = VISIBLE
                                    documentProblems.text = getString(R.string.no_connection)
                                }
                                else
                                    documentProblems.visibility = GONE
                            }
                        }
                        it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    } else {
                        documentAnalysis.visibility = GONE
                        it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    }
                }

//                buttonUserSkills.setOnClickListener {
//                    isSkillAnalysisOpen = !isSkillAnalysisOpen
//
//                    TransitionManager.beginDelayedTransition(binding.root)
//                    if (!isSkillAnalysisOpen) {
//                        lineChartUserSkills.visibility = VISIBLE
////                        lineChartUserSkills.animateXY(500, 1300, Easing.EaseInOutBounce, Easing.EaseInExpo)
//                        lineChartUserSkills.invalidate()
//                        it.setBackgroundResource(R.drawable.open_list_with_arrow)
//                    }
//                    else{
//                        lineChartUserSkills.visibility = GONE
//                        it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
//
//                    }
//                }
            }

            //Получение аналитических данных

        }
    }
    fun getColor(context: Context, colorResId: Int): Int {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
    fun <T> LiveData<T>.observeOneData(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        var previousKey: Any? = value?: "NULL"
        observe(lifecycleOwner){
            if(previousKey == "NULL"){
                previousKey = value
                observer.onChanged(value!!)
            }
        }
    }
}