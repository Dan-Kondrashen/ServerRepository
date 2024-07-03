package ru.kondrashen.diplomappv20.presentation.fragments

// Импорты библиотек, необходимых для работы приложения

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.ExtrSkillsActualPopularityGrafFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.SingleLiveEvent
import java.sql.Date
import java.sql.Time
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

// Класс фрагмента, реализующий сбор статистики

class GraphMainFragment : Fragment() {
    private var userId: Int? = null
    private var userType: String? = null
    private var type: String = ""
    private var mod: String = "all"
    private var flag: Int = 0
    private var startedDate: String? = null
    private var endDate: String? = null
    private var skillFamilyId: Int? = null
    private var skillId: Int? = null
    private var labels = mutableListOf<String>()
    private var BarLabels = mutableListOf<String>()
    private var barDataSetList: MutableList<IBarDataSet> = mutableListOf()
    private lateinit var dataSet: ILineDataSet
    private var dataSetList: MutableList<ILineDataSet> = mutableListOf()
    private var dataViews: MutableList<Entry> = mutableListOf()
    private val item: MutableLiveData<String> = MutableLiveData("")
    private var skillTypeAdapter: ArrayAdapter<String>? = null
    private var skillAdapter: ArrayAdapter<String>? = null
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private lateinit var userModel: UserAccountControlViewModel
    private var isPrefOpen: Boolean = false
    private var isPopularityOpen: Boolean = true
    private var isImportanceOpen: Boolean = true
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private var text: String = ""
    private var _binding: ExtrSkillsActualPopularityGrafFragmentBinding? = null
    private val binding get() = _binding!!
    private var calendar = Calendar.getInstance()
    private var calendarEnd = Calendar.getInstance()


    companion object {
        private const val TAG = "AnalysisFragment-Main"
        fun newInstance(userId: Int, userType: String) = GraphMainFragment().apply {
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
        _binding = ExtrSkillsActualPopularityGrafFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extraInfoModel.getStartSkillDataFromServ("exists")
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        }
        // Добавление проверки жизненного цикла фрагментов
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            onBackPressedCallback
        )
        binding.lineChartSkillPopularity.apply {

            xAxis.granularity = 1f;
            animateY(1000)
            xAxis.setCenterAxisLabels(true)
            xAxis.textSize = 10f
            axisRight.isEnabled = false
            setExtraOffsets(10f,10f,10f,10f)
            setBackgroundResource(R.drawable.border_for_document_maininfo)
            description.isEnabled = false
            legend.apply {
                xEntrySpace = 3f
                yEntrySpace = 5f
                setDrawInside(false)
                entries
                isWordWrapEnabled = true
            }
        }
        binding.barChartSkillPopularity.apply {
            var xaxis = xAxis
            xaxis.spaceMax = 1.65f
            xAxis.granularity = 1f
            animateY(1000)
            animateX(1250)
            xAxis.setCenterAxisLabels(true)
            xAxis.textSize = 10f
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            setDrawGridBackground(false)
            setBackgroundResource(R.drawable.border_for_document_maininfo)
            setExtraOffsets(0f,10f,10f,10f)
            description.isEnabled = false
            legend.apply {
                xEntrySpace = 3f
                yEntrySpace = 5f
                setDrawInside(false)
                isWordWrapEnabled = true
            }

        }
        binding.apply {
            saveImportanceGraphBtn.setOnClickListener {
                barChartSkillPopularity
                    .saveToGallery("importance graph of " +
                            "${Date(java.util.Date().time)} ${Time(java.util.Date().time).toString()
                                .replace(":",".")}.jpg", 85)
            }
            savePopularityGraphBtn.setOnClickListener {
                lineChartSkillPopularity
                    .saveToGallery("skill popularity graph of " +
                            "${Date(java.util.Date().time)} ${Time(java.util.Date().time).toString()
                                .replace(":",".")}.jpg", 85)
            }
        }

        binding.functionalAnalysisBlock.apply {
            choseSkillForAnalysisQuestion.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    choseSkillView.visibility = VISIBLE

                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    choseSkillView.visibility = GONE
                }
            }
            choseDateInterval.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    intervalInfoView.visibility = VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    intervalInfoView.visibility = GONE
                }
            }
            callAnaliticPreference.setOnClickListener {
                isPrefOpen = !isPrefOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if(!isPrefOpen){
                    analysisPref.visibility = VISIBLE
                    it.setBackgroundResource(R.drawable.open_list_with_arrow)
                }
                else{
                    analysisPref.visibility = GONE
                    it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                }
            }

            skillAdapter = ArrayAdapter(requireContext(), R.layout.spiner_dropdown_item, listOf())
            updateUI()
        }
    }

    private fun updateUI() {
        if (isAdded) {
            calendar.time = Date(format.parse("2000-01-01 00:00:00").time)

            val array = requireContext().resources.getStringArray(R.array.skill_types)
            val arrayDate = requireContext().resources.getStringArray(R.array.date_types)
            extraInfoModel.getSkillTypesFromServ("exists")
            binding.functionalAnalysisBlock.skillTypeSelector.adapter =
                ArrayAdapter(requireActivity(),
                    R.layout.spiner_dropdown_item,
                    array)
            binding.functionalAnalysisBlock.dateIntervalType.adapter =
                ArrayAdapter(requireActivity(),
                    R.layout.spiner_dropdown_item,
                    arrayDate)
            extraInfoModel.getSkillTypeNamesFromRoom().observe(viewLifecycleOwner){
                skillTypeAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, it)
                binding.functionalAnalysisBlock.skillClass.adapter = skillTypeAdapter
            }
            val observer = object : Observer<List<String>>{
                override fun onChanged(value: List<String>) {
                    skillAdapter =
                        ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, value)
                    binding.functionalAnalysisBlock.skillChose.adapter = skillAdapter
                }
            }
            binding.functionalAnalysisBlock.apply {
                skillChose.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if(choseSkillForAnalysisQuestion.isChecked){
                            extraInfoModel.getSkillIdBySkillNameFromRoom(type, skillChose.selectedItem.toString()).observe(viewLifecycleOwner){
                                skillId = it
                                println(it)
                            }
                        }
                        else
                            skillId = null
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            }
            binding.apply {

                buttonPopularity.setOnClickListener {
                    if (!binding.functionalAnalysisBlock.choseSkillForAnalysisQuestion.isChecked){
                        skillId = null
                    }
                    else {
                        extraInfoModel.getSkillIdBySkillNameFromRoom(
                            type,
                            binding.functionalAnalysisBlock.skillChose.selectedItem.toString()
                        ).observeOneData(viewLifecycleOwner) {skill->
                            skillId = skill
                        }
                    }

                    dataSetList.clear()
                    isPopularityOpen = !isPopularityOpen
                    TransitionManager.beginDelayedTransition(binding.root)
                    if (!isPopularityOpen){
                        if (flag == 0) {
                            lineChartSkillPopularity.visibility = GONE
                        }
                        else
                            lineChartSkillPopularity.visibility = VISIBLE

                        var date =
                            if (binding.functionalAnalysisBlock.choseDateInterval.isChecked)
                                Date(format.parse(startedDate?: "2000-01-01 00:00:00").time)
                            else
                                Date(format.parse("2000-01-01 00:00:00").time)
                        var date2 =
                            if (binding.functionalAnalysisBlock.choseDateInterval.isChecked)
                                Date(format.parse(endDate?: "${calendarEnd.get(Calendar.YEAR)+1}-01-01 00:00:00").time)
                            else
                                Date(calendarEnd.time.time)

                        extraInfoModel.getAnalyticDataFromRoom(mod,
                            date,
                            date2,
                            skillId=skillId,
                            skillFamilyId = skillFamilyId?:0,
                            type).observeOneData(viewLifecycleOwner){ data->
                            progressPopularity.visibility = GONE
                            if (data.isEmpty()) {
                                lineChartNoData.visibility = VISIBLE
                                lineChartSkillPopularity.visibility = GONE
                                if (flag == 0) {
                                    lineChartNoData.text = getString(R.string.analyticProbBeforeGet)
                                }
                                else {
                                    if(type == "knowledge")
                                        lineChartNoData.text =
                                            getString(R.string.knowGraphProblem)
                                    else
                                        lineChartNoData.text =
                                            getString(R.string.specGrafProblem)
                                }
                            }
                            else{
                                lineChartSkillPopularity.visibility = VISIBLE
                                var items = data.filter { it.respType=="view"}
                                if (items.isNotEmpty()) {
                                    lineChartNoData.visibility = GONE

                                }
                                else{
                                    lineChartNoData.visibility = VISIBLE
                                    lineChartSkillPopularity.visibility = GONE
                                }
                            }
                            extraInfoModel.getAnalyticsNames(type, skillFamilyId?:0).observeOneData(viewLifecycleOwner){ skillNames ->
                                var index = 0
                                var colors = getColorsForGraph(skillNames.size, requireContext().theme)
                                for(skillName in skillNames){
                                    var items = data.filter { (it.respType=="view" && it.skillName==skillName)}
                                    if (items.isNotEmpty()){
                                        dataViews= items.map {  analyticInfo ->
                                            Entry(labels.indexOf(analyticInfo.date).toFloat(), analyticInfo.numUsage.toFloat()) } as MutableList<Entry>
                                        dataSet = LineDataSet(dataViews, skillName)
                                        (dataSet as LineDataSet).setCircleColor(colors[skillNames.indexOf(skillName)])
                                        (dataSet as LineDataSet).color = colors[skillNames.indexOf(skillName)]

                                        dataSetList.add(index, dataSet)
                                        index +=1
                                    }
                                }
                                if (dataSetList.isNotEmpty())
                                    savePopularityGraphBtn.visibility = VISIBLE
                                else
                                    savePopularityGraphBtn.visibility = GONE


                                var resultData = LineData(dataSetList)
                                binding.lineChartSkillPopularity.data = resultData

                                binding.lineChartSkillPopularity.invalidate()
                            }
                        }
                        lineChartSkillPopularity.animateX(700, Easing.EaseInOutBounce)
                        lineChartSkillPopularity.invalidate()
                        it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    }
                    else{
                        lineChartSkillPopularity.visibility = GONE
                        savePopularityGraphBtn.visibility = GONE
                        lineChartNoData.visibility = GONE
                        it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    }
                }

                buttonImportance.setOnClickListener {
                    isImportanceOpen = !isImportanceOpen
                    barDataSetList.clear()
                    TransitionManager.beginDelayedTransition(binding.root)
                    if (!isImportanceOpen) {
                        if (flag == 0) {
                            barChartNoData.visibility = VISIBLE
                        }
                        else {
//                            progressImportance.visibility = VISIBLE
                            barChartNoData.visibility = GONE
                            barChartSkillPopularity.visibility = VISIBLE
                        }
                        var date =  Date(format.parse("2023-01-01 00:00:00").time)
                        var date2 =  Date(format.parse("2025-01-01 00:00:00").time)
                        val items1 = mutableListOf<BarEntry>()
                        val items2 = mutableListOf<BarEntry>()
                        val items3 = mutableListOf<BarEntry>()
                        if (!binding.functionalAnalysisBlock.choseSkillForAnalysisQuestion.isChecked){
                            skillId = null
                        }
                        else {
                            extraInfoModel.getSkillIdBySkillNameFromRoom(
                                type,
                                binding.functionalAnalysisBlock.skillChose.selectedItem.toString()
                            ).observeOneData(viewLifecycleOwner) {skill->
                                skillId = skill
                            }
                        }
                        extraInfoModel.getBarAnalyticDataFromRoom(
                            date,
                            date2,
                            skillId=skillId,
                            skillFamilyId = skillFamilyId?:0,
                            type).observeOneData(viewLifecycleOwner) { data ->

                            if (data.isNotEmpty()) {
                                progressImportance.visibility = GONE
                                var barLabels =
                                    data.sortedByDescending { (it.numUsage) }.map { it.skillName }
                                        .distinct() as MutableList<String>
                                binding.barChartSkillPopularity.apply {
                                    xAxis.valueFormatter = IndexAxisValueFormatter(barLabels)
                                    xAxis.axisLineWidth = 1f
                                }
                                var j = 0
                                for (skillName in barLabels) {
                                    val i = barLabels.indexOf(skillName)
                                    val itemsView =
                                        data.find { (it.respType == "view" && it.skillName == skillName) }
                                    items1.add(
                                        BarEntry(
                                            i.toFloat(),
                                            itemsView?.numUsage?.toFloat() ?: 0.1f
                                        )
                                    )
                                    val itemsResp =
                                        data.find { (it.respType == "response" && it.skillName == skillName) }
                                    items2.add(
                                        BarEntry(
                                            i.toFloat(),
                                            itemsResp?.numUsage?.toFloat() ?: 0.1f
                                        )
                                    )
                                    val itemsDis =
                                        data.find { (it.respType == "dismiss" && it.skillName == skillName) }
                                    items3.add(
                                        BarEntry(
                                            j.toFloat(),
                                            itemsDis?.numUsage?.toFloat() ?: 0.1f
                                        )
                                    )
                                    if (itemsResp == null && itemsView == null && itemsDis == null)
                                        saveImportanceGraphBtn.visibility = GONE
                                    else
                                        saveImportanceGraphBtn.visibility = VISIBLE
                                    j += 3
                                }
                                val set1 = BarDataSet(items1, getString(R.string.numOfView))
                                val typedValue = TypedValue()
                                val theme = requireActivity().theme
                                theme.resolveAttribute(
                                    androidx.appcompat.R.attr.colorPrimaryDark,
                                    typedValue,
                                    true
                                )
                                var color = typedValue.data
                                set1.color = color
                                val set2 = BarDataSet(items2, getString(R.string.numOfResp))
                                set2.color = getColor(requireActivity(), R.attr.colorSuccess)

                                val set3 = BarDataSet(items3, getString(R.string.numOfDismiss))
                                set3.color = getColor(requireActivity(), R.attr.colorAlertDark)

                                barDataSetList.add(set1)
                                barDataSetList.add(set2)
                                barDataSetList.add(set3)
                                binding.barChartSkillPopularity.apply {
                                    this.data = BarData(barDataSetList)
                                    this.data.barWidth = 0.30F
                                    groupBars(0F, 0.06F, 0.02F)

                                    invalidate()

                                }
                            }
                            else {
                                barChartNoData.visibility = VISIBLE
                                progressImportance.visibility = GONE
                                barChartSkillPopularity.visibility = GONE
                                if (flag == 0) {
                                    barChartNoData.text = getString(R.string.analyticProbBeforeGet)
                                }
                                else {
                                    if(type == "knowledge")
                                        barChartNoData.text =
                                            getString(R.string.knowGraphProblem)
                                    else
                                        barChartNoData.text =
                                            getString(R.string.specGrafProblem)
                                }
                            }
                        }

                        barChartSkillPopularity.animateXY(500, 1300, Easing.EaseInOutBounce, Easing.EaseInExpo)
                        barChartSkillPopularity.invalidate()
                        it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    }
                    else{
                        barChartNoData.visibility = GONE
                        barChartSkillPopularity.visibility = GONE
                        progressImportance.visibility = GONE
                        saveImportanceGraphBtn.visibility = GONE
                        it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    }
                }
            }
            binding.functionalAnalysisBlock.skillClass.apply {
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (binding.functionalAnalysisBlock.skillTypeSelector.selectedItem.toString() == array[0]){
                            type = "knowledge"
                        }
                        else{
                            type = "specialization"
                        }
                        item.postValue(selectedItem.toString())

                        extraInfoModel.getSkillTypeIdByNameFromRoom(selectedItem.toString()).observe(viewLifecycleOwner){skillId1->
                            if (isAdded) {
                                if (skillId1 != null) {
                                    skillFamilyId = skillId1
                                    extraInfoModel.getSkillItemsBySkillTypeAndClassFromServ(
                                        type,
                                        skillId1
                                    )
                                }
                            }
                        }
//                        var liveData = extraInfoModel.getSkillsNamesBySkillTypeAndClass(type, selectedItem.toString())
//                        var singletonLiveData = liveData.toSingleton()
                        extraInfoModel.getSkillsNamesBySkillTypeAndClass(type, selectedItem.toString()).removeObservers(viewLifecycleOwner)
                        extraInfoModel.getSkillsNamesBySkillTypeAndClass(type, selectedItem.toString()).observeOneData(viewLifecycleOwner, observer)

                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            }
            binding.functionalAnalysisBlock.skillTypeSelector.apply {
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        type = when(selectedItem.toString()){
                            array[0] -> "knowledge"
                            else -> "specialization"
                        }
                        if (binding.functionalAnalysisBlock.skillClass.selectedItem != null) {
                            val i =
                                binding.functionalAnalysisBlock.skillClass.selectedItem.toString()
                            extraInfoModel.getSkillsNamesBySkillTypeAndClass(type, i).removeObservers(viewLifecycleOwner)
                            extraInfoModel.getSkillsNamesBySkillTypeAndClass(type, i)
                                .observeOneData(viewLifecycleOwner, observer)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            }
            binding.functionalAnalysisBlock.dateIntervalType.apply {
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        mod = when(selectedItem.toString()){
                            arrayDate[1] -> "year"
                            arrayDate[2] -> "month"
                            arrayDate[3] -> "day"
                            else -> "all"
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            }
            // Получение начальной даты сбора статистики
            binding.functionalAnalysisBlock.dateStart.setOnClickListener {
                val manager =parentFragmentManager
                when(mod){
                    "month" -> {
                        val dialog = MonthYearPickerDialogFragment(mod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateStart.text = startedDate
                        }
                    }
                    "day" -> {
                        val dialog = DateDialogFragment(mod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateStart.text = startedDate
                        }
                    }
                    "year" -> {
                        val dialog = MonthYearPickerDialogFragment(mod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateStart.text = startedDate
                        }
                    }
                    else-> {
                        val dialog = DateDialogFragment(mod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateStart.text = startedDate
                        }
                    }
                }
            }
            binding.functionalAnalysisBlock.dateEnd.setOnClickListener {
                val manager =parentFragmentManager
                when(mod){
                    "month" -> {
                        val dialog = MonthYearPickerDialogFragment(mod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateEnd.text = endDate
                        }
                    }
                    "day" -> {
                        val dialog = DateDialogFragment(mod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateEnd.text = endDate
                        }
                    }
                    "year" -> {
                        val dialog = MonthYearPickerDialogFragment(mod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateEnd.text = endDate
                        }
                    }
                    else-> {
                        val dialog = DateDialogFragment(mod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(PublicConstants.DATE, this) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            binding.functionalAnalysisBlock.dateEnd.text = endDate
                        }
                    }
                }
            }
            //Получение аналитических данных

            binding.functionalAnalysisBlock.getAnaliticBtn.setOnClickListener {
                var month= ""
                var day =""
                val cal: Calendar = Calendar.getInstance()
                flag = 1
                isPrefOpen = !isPrefOpen
                isPopularityOpen = true
                TransitionManager.beginDelayedTransition(binding.root)
                binding.buttonPopularity.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                TransitionManager.beginDelayedTransition(binding.root)
                binding.lineChartSkillPopularity.visibility = GONE
                binding.savePopularityGraphBtn.visibility = GONE
                binding.functionalAnalysisBlock.analysisPref.visibility = GONE
                binding.functionalAnalysisBlock.callAnaliticPreference.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                var bar = Snackbar.make(requireActivity().findViewById(android.R.id.content), text, PublicConstants.DELAY)
                bar.anchorView= binding.scrollPage
                userId = arguments?.getInt(PublicConstants.USER_ID)
                userType = arguments?.getString(PublicConstants.USER_TYPE)
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                extraInfoModel.getSkillDataFromServ(mod,
                    Date(format.parse("2000-01-01 00:00:00").time),
                    Date(format.parse("${cal.get(Calendar.YEAR)+1}-01-01 00:00:00").time),
                    null,skillFamilyId = skillFamilyId,
                    type).observe(viewLifecycleOwner){
                    if (it != "success") {
                        if (it.contains("Server response")) {
                            bar.setText(R.string.dismis_connection)
                            bar.show()
                        }
                        else if (it == "No connection to server") {
                            bar.setText(R.string.no_connection)
                            bar.show()
                        }
                    }

                    if (binding.functionalAnalysisBlock.choseDateInterval.isChecked) {
                        var date =
                            if (binding.functionalAnalysisBlock.choseDateInterval.isChecked)
                                Date(format.parse(startedDate?: "2000-01-01 00:00:00").time)
                            else
                                Date(format.parse("2000-01-01 00:00:00").time)
                        var date2 =
                            if (binding.functionalAnalysisBlock.choseDateInterval.isChecked)
                                Date(format.parse(endDate?: "${calendarEnd.get(Calendar.YEAR)+1}-01-01 00:00:00").time)
                            else
                                Date(calendarEnd.time.time)
                        calendar.time =date
                        calendarEnd.time =date2
                    }
                    else
                        calendar.time = Date(format.parse("2000-01-01 00:00:00").time)


                    when(mod) {
                        "year" -> {
                            calendar.add(Calendar.YEAR, -1)
                        }
                        "month" -> calendar.add(Calendar.MONTH, -1)
                    }
                    if (this::dataSet.isInitialized){
                        dataSet.clear()
                    }

                    // Очистка старых данных графика

                    labels.clear()


                    // Определение способа отображения данных на графике

                    when(mod) {
                        "year" -> {
                            while (calendar.get(Calendar.YEAR) < calendarEnd.get(Calendar.YEAR)) {
                                calendar.add(Calendar.YEAR, 1)
                                labels.add(calendar.get(Calendar.YEAR).toString())
                            }
                        }

                        "month" -> {
                            while (calendar.get(Calendar.MONTH) < calendarEnd.get(Calendar.MONTH) ||
                                calendar.get(Calendar.YEAR) < calendarEnd.get(Calendar.YEAR)) {
                                calendar.add(Calendar.MONTH, 1)
                                month = when((calendar.get(Calendar.MONTH)+1).toString().length){
                                    1 -> "0${(calendar.get(Calendar.MONTH)+1)}"
                                    else -> calendar.get(Calendar.MONTH).toString()
                                }
                                labels.add("${calendar.get(Calendar.YEAR)}-$month")
                            }
                            println(labels)
                        }
                        else -> {
                            while (calendar.get(Calendar.DATE) < calendarEnd.get(Calendar.DATE) ||
                                calendar.get(Calendar.MONTH) < calendarEnd.get(Calendar.MONTH) ||
                                calendar.get(Calendar.YEAR) < calendarEnd.get(Calendar.YEAR)) {
                                calendar.add(Calendar.DATE, 1)
                                month = when((calendar.get(Calendar.MONTH)+1).toString().length){
                                    1 -> "0"+(calendar.get(Calendar.MONTH)+1).toString()
                                    else -> calendar.get(Calendar.MONTH).toString()
                                }
                                day = when(calendar.get(Calendar.DATE).toString().length){
                                    1 -> "0"+calendar.get(Calendar.DATE).toString()
                                    else -> calendar.get(Calendar.DATE).toString()
                                }
                                labels.add("${calendar.get(Calendar.YEAR)}-$month-$day")
                            }
                        }
                    }

                    // Назначение меток для графика

                    binding.lineChartSkillPopularity.xAxis.setCenterAxisLabels(true)
                    binding.lineChartSkillPopularity.xAxis.valueFormatter= IndexAxisValueFormatter(labels)
                }
            }
        }

    }

    fun getColor(context: Context, colorResId: Int): Int {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
    fun getColorsForGraph(numberOfColors: Int, theme: Resources.Theme): List<Int> {
        val colors = mutableSetOf<Int>()
        val typedValue = TypedValue()
        val themeColorHue = if (theme.resolveAttribute(R.attr.colorPrimaryLight, typedValue, true)) {
            // Получаем цвет из темы и конвертируем его в формат HSV
            val hsv = FloatArray(3)
            Color.colorToHSV(typedValue.data, hsv)
            hsv[0] // Возвращаем оттенок
        } else {
            throw IllegalArgumentException("Theme does not contain specified color")
        }

        while (colors.size < numberOfColors) {
            val hue = Random.nextFloat() * 360 // Случайный оттенок от 0 до 360
            val saturation = 0.5f + Random.nextFloat() * 0.5f // Насыщенность от 0.5 до 1
            val brightness = 0.5f + Random.nextFloat() * 0.5f // Яркость от 0.5 до 1

            // Проверяем, не попадает ли сгенерированный оттенок в диапазон исключения
            if (!(hue >= themeColorHue - 10 && hue <= themeColorHue + 10)) {
                val color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
                colors.add(color) // Добавляем цвет, если он не входит в диапазон исключения
            }
        }
        return colors.toList()
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
    fun <T> LiveData<T>.toSingleton(): SingleLiveEvent<T> {
        val mediatorLiveData = MediatorLiveData<T>()
        val singletonLiveData = SingleLiveEvent<T>()
        mediatorLiveData.addSource(this) {
            singletonLiveData.value = it
        }
        return singletonLiveData
    }
}