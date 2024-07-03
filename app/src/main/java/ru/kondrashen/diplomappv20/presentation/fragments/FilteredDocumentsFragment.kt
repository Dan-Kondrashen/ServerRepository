package ru.kondrashen.diplomappv20.presentation.fragments
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.FilterableDocumentsPageBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.activitys.AuthActivity
import ru.kondrashen.diplomappv20.presentation.adapters.DocumentListFilteredAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleKnowledgeAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.SearchableBase
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentPreference
import java.sql.Date
import java.util.Calendar
import java.util.Locale


class FilteredDocumentsFragment : Fragment(), onItemClickInterface<User> {
    private var _binding: FilterableDocumentsPageBinding? = null
    private var adapter: DocumentListFilteredAdapter? = null
    private var knowAdapter: SimpleKnowledgeAdapter? = null
    private var knowTypeAdapter: ArrayAdapter<String>? = null
    private var documentTypeAdapter: ArrayAdapter<String>? = null
    private var documentType: String? = "vacancy"
    private var mod: String = "date"
    private var dateMod: String = "date"
    private var startedDate: String? = null
    private var endDate: String? = null
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private var userId: Int? = null
    private var respId: Int? = null
    private var skillId: Int? = null
    private var type: String? = null
    private var items = mutableListOf<DocumentInfoWithKnowledge>()
    private var isPrefOpen: Boolean = false
    private var isExtraPrefOpen: Boolean = false
    private var calendar = Calendar.getInstance()
    private var calendarEnd = Calendar.getInstance()
    private lateinit var userAccountModel: UserAccountControlViewModel
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private lateinit var dataModel: MainPageViewModel
    private val binding get() = _binding!!
    var menuProviderMain = object: MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_go_to_account -> {
                    val bundle = Bundle()
                    userId = arguments?.getInt("userId")
                    bundle.putInt("userId", userId?: 0)
                    findNavController().navigate(R.id.action_mainFragment_to_personalSpaceFragment, bundle)
                    true
                }

                R.id.action_settings -> {
                    findNavController().navigate(R.id.action_mainFragment_to_preferenceFragment)
                    true
                }
                R.id.action_logout -> {
                    val intent = AuthActivity.newIntent(requireActivity())
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        requireActivity().finish()
                        startActivity(intent)
                    }
                    true
                }
                else -> false
            }
        }
    }
    companion object {
        private const val TAG = "FilteredDocumentsFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
        userId = arguments?.getInt(PublicConstants.USER_ID)
        userId?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FilterableDocumentsPageBinding.inflate(inflater, container, false)
        updateUI()
        val menuHost: MenuHost = requireActivity()
        menuHost.removeMenuProvider(FilteredDocumentsFragment().menuProviderMain)
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        val bundle = Bundle()
                        userId = arguments?.getInt(PublicConstants.USER_ID)
                        bundle.putInt(PublicConstants.USER_ID, userId?: 0)
                        if (userId !=0 && respId !=0)
                            findNavController().navigate(R.id.action_filtrationFragment_to_preferenceFragment, bundle)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = arguments?.getInt(PublicConstants.USER_ID)
        extraInfoModel.getStartSkillDataFromServ("exists")
        userId?.let { uId ->
            userAccountModel.getUserData(2).observe(viewLifecycleOwner) {
                documentType = when (it.roleId) {
                    2 -> "resume"
                    1 -> "vacancy"
                    else -> "vacancy"
                }
            }
            binding.documentsPref.callDocuments.setOnClickListener {
                isPrefOpen = false
                TransitionManager.beginDelayedTransition(binding.root)
                binding.documentsPref.apply {
                    documentsPrefBlock.visibility = GONE
                    callDocuments.visibility = GONE
                    callDocumentsPreference.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    callDocumentsPreference.text = getString(R.string.show_parameters)
                }
                var number: Int? = null

                var salaryF: Float? = null
                var salaryS: Float? = null
                binding.documentsPref.apply {

                    mod = if (inversionSwitch.isChecked) {
                        "asc"
                    } else {
                        "desc"
                    }
                    if (documentNumQuestion.isChecked) {
                        val num = etNumDocuments.text.toString()
                        if (num != "0" && num != "")
                            number = num.toInt()
                        else {
                            number = null
                            val bar = Snackbar.make(
                                binding.root as ViewGroup,
                                getText(R.string.not_right_number),
                                Snackbar.LENGTH_LONG
                            )
                            bar.show()
                        }
                    }
                    else
                        number = null
                    if (documentSalaryQuestion.isChecked) {
                        val num = etSalaryStart.text.toString()
                        val num2 = etSalaryEnd.text.toString()
                        if (num != "0" && num != "" && num2 != "0" && num2 != "") {
                            salaryF = num.toFloat()
                            salaryS = num2.toFloat()
                        } else if (num != "0" && num != "")
                            salaryF = num.toFloat()
                        else if (num2 != "0" && num2 != "")
                            salaryS = num2.toFloat()
                        else {
                            val bar = Snackbar.make(
                                binding.root as ViewGroup,
                                getText(R.string.not_right_salary),
                                Snackbar.LENGTH_LONG
                            )
                            bar.show()
                        }
                    }
                    if (choseDateIntervalQuestion.isChecked) {
                        var dateStartInf =
                            if (dateStart.text.toString() != getString(R.string.date_not_chosen))
                                Date(format.parse(startedDate ?: "2000-01-01 00:00:00").time)
                            else
                                Date(format.parse("2000-01-01 00:00:00").time)
                        var dateEndInf =
                            if (dateEnd.text.toString() != getString(R.string.date_not_chosen))
                                Date(
                                    format.parse(
                                        endDate
                                            ?: "${calendarEnd.get(Calendar.YEAR) + 1}-01-01 00:00:00"
                                    ).time
                                )
                            else
                                Date(calendarEnd.time.time)
                        calendar.time = dateStartInf
                        calendarEnd.time = dateEndInf
                        println(dateStartInf.toString() + "dateS")
                        println(dateEndInf.toString() + "dateE")
                    }
                    val dateStart = if (choseDateIntervalQuestion.isChecked)
                        startedDate
                    else
                        null
                    val dateEnd = if (choseDateIntervalQuestion.isChecked)
                        endDate
                    else
                        null
                    println(number.toString() + " num")
                    var knowIdList = if (documentKnowledgeQuestion.isChecked){
                        knowAdapter?.getCheckedItems()
                    } else {
                        null
                    }
                    val docPref = DocumentPreference(
                        type ?: "date",
                        salaryF,
                        salaryS,
                        dateStart,
                        dateEnd,
                        knowIdList,
                        number
                    )
                    val bar = Snackbar.make(
                        binding.root as ViewGroup,
                        "",
                        Snackbar.LENGTH_LONG
                    )
                    binding.documentRecyclerView.visibility = GONE
                    binding.progressBlock.visibility = VISIBLE
                    documentType?.let { typeLoc ->
                        dataModel.getFilteredDocumentsFromServ(uId, typeLoc, mod, docPref).observe(viewLifecycleOwner){ resp ->
                            if (resp != "" && resp != null) {
                                binding.progressBlock.visibility = GONE
                                dataModel.getFilteredDocumentsFromRoom(
                                    typeLoc,
                                    mod,
                                    docPref
                                ).observeOneData(viewLifecycleOwner) { docList ->
                                    if (docList.isNotEmpty()) {
                                        binding.documentRecyclerView.visibility = VISIBLE
                                        adapter = DocumentListFilteredAdapter(
                                            docList as MutableList<DocumentInfoWithKnowledge>,
                                            uId,
                                            typeLoc,
                                            mod,
                                            findNavController(),
                                            requireActivity(),
                                            docPref
                                        )
                                        binding.documentRecyclerView.adapter = adapter
                                    }
                                }
                            }
                            when(resp){
                                "success" ->{
                                    bar.setText(R.string.success)
                                    bar.show()
                                }
                                "no items" ->{
                                    bar.setText(R.string.no_items_by_filter)
                                    bar.show()
                                }
                                "server not available"->{
                                    bar.setText(R.string.server_not_avilable)
                                    bar.show()
                                }
                                "query problem"->{
                                    bar.setText(R.string.problem_query)
                                    bar.show()
                                }
                                "timeout"->{
                                    bar.setText(R.string.no_connection)
                                    bar.show()
                                }
                            }

                        }
                        //заменить на typeLoc

                    }
                }


            }
        }

        binding.documentsPref.apply {
            etNumDocuments.transformationMethod = null
            addPref.setOnClickListener {
                isExtraPrefOpen = !isExtraPrefOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (isExtraPrefOpen) {
                    extraPref.visibility = VISIBLE
                } else {
                    extraPref.visibility = GONE
                }
            }
            callDocumentsPreference.setOnClickListener {
                isPrefOpen = !isPrefOpen
                TransitionManager.beginDelayedTransition(binding.root)
                if (isPrefOpen) {
                    documentsPrefBlock.visibility = VISIBLE
                    callDocuments.visibility = VISIBLE
                    callDocumentsPreference.setBackgroundResource(R.drawable.open_list_with_arrow)
                    callDocumentsPreference.text = getString(R.string.hide_parameters)
                } else {
                    documentsPrefBlock.visibility = GONE
                    callDocuments.visibility = GONE
                    callDocumentsPreference.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    callDocumentsPreference.text = getString(R.string.show_parameters)
                }
            }
            documentNumQuestion.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    documentNumberView.visibility = VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    documentNumberView.visibility = GONE
                }
            }
            documentSalaryQuestion.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    documentSalaryView.visibility = VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    documentSalaryView.visibility = GONE
                }
            }
            documentKnowledgeQuestion.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    documentKnowledgeView.visibility = VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    documentKnowledgeView.visibility = GONE
                }
            }
            choseDateIntervalQuestion.setOnCheckedChangeListener{ buttonView, isChecked ->
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(binding.root)
                    intervalInfoView.visibility = VISIBLE
                } else {
                    TransitionManager.beginDelayedTransition(binding.root)
                    intervalInfoView.visibility = GONE
                }
            }
            searchBar.setOnSearchClickListener {
                val animation = AnimationUtils.loadAnimation(context, R.anim.scale_in_searchview)
                searchBar.startAnimation(animation)
            }

            searchBar.setOnQueryTextFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    val animation =
                        AnimationUtils.loadAnimation(context, R.anim.scale_out_searchview)
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {
                        }

                        override fun onAnimationEnd(animation: Animation?) {
                            searchBar.clearAnimation()
                            searchBar.isIconified = true
                        }

                        override fun onAnimationRepeat(animation: Animation?) {
                        }
                    })
                    if (!searchBar.isIconified and (searchBar.query.toString() == ""))
                        searchBar.startAnimation(animation)
                }
            }
            val btnClose =
                searchBar.rootView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            btnClose.setOnClickListener {
                val animation = AnimationUtils.loadAnimation(context, R.anim.scale_out_searchview)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        searchBar.clearAnimation()
                        searchBar.isIconified = true
                    }

                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                })
                if (!searchBar.isIconified)
                    searchBar.startAnimation(animation)
            }
            searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(newText: String?): Boolean {
                    SearchableBase().filterAdapters(newText, adapter)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    SearchableBase().filterAdapters(newText, adapter)
                    return true
                }
            })

        }
    }

    private fun updateUI() {
        extraInfoModel.getSkillTypesFromServ("exists")
        val arrayDate = requireContext().resources.getStringArray(R.array.date_types)
        val documentTypeArray =
            requireContext().resources.getStringArray(R.array.pref_document_type)
        extraInfoModel.getSkillTypeNamesFromRoom().observe(viewLifecycleOwner){
            knowTypeAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, it)
            binding.documentsPref.knowTypeSelector.adapter = knowTypeAdapter
        }
        documentTypeAdapter =
            ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, documentTypeArray)
        userId = arguments?.getInt(PublicConstants.USER_ID)
        binding.documentsPref.apply {
            filterTypeSelector.adapter = documentTypeAdapter
            filterTypeSelector.onItemSelectedListener = object : OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    type = when (filterTypeSelector.selectedItem.toString()) {
                        documentTypeArray[0] -> "date"
                        documentTypeArray[1] -> "number"
                        documentTypeArray[2] -> "wage"
                        documentTypeArray[3] -> "level"
                        else -> "date"
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
            knowTypeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    extraInfoModel.getKnowledgeBySkillClass(knowTypeSelector.selectedItem.toString()).observeOneData(viewLifecycleOwner){
                        knowAdapter = SimpleKnowledgeAdapter(it as MutableList<Knowledge>, "checkedView", null)
                        knowledgeChoseRecycle.adapter = knowAdapter
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            dateIntervalType.adapter =
                ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, arrayDate)
            dateIntervalType.apply {
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            dateMod = when (selectedItem.toString()) {
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
            dateStart.setOnClickListener {
                val manager = parentFragmentManager
                when (dateMod) {
                    "month" -> {
                        val dialog =
                            MonthYearPickerDialogFragment(dateMod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            dateStart.text = startedDate
                        }
                    }

                    "day" -> {
                        val dialog = DateDialogFragment(dateMod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            dateStart.text = startedDate
                        }
                    }

                    "year" -> {
                        val dialog =
                            MonthYearPickerDialogFragment(dateMod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            dateStart.text = startedDate
                        }
                    }

                    else -> {
                        val dialog = DateDialogFragment(dateMod, "start", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            startedDate = bundle.getString(requestKey).toString()
                            dateStart.text = startedDate
                        }
                    }
                }
            }
            dateEnd.setOnClickListener {
                val manager = parentFragmentManager
                when (dateMod) {
                    "month" -> {
                        val dialog = MonthYearPickerDialogFragment(dateMod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            dateEnd.text = endDate
                        }
                    }

                    "day" -> {
                        val dialog = DateDialogFragment(dateMod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            dateEnd.text = endDate
                        }
                    }

                    "year" -> {
                        val dialog = MonthYearPickerDialogFragment(dateMod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            dateEnd.text = endDate
                        }
                    }

                    else -> {
                        val dialog = DateDialogFragment(dateMod, "end", startedDate, endDate)
                        dialog.show(manager, PublicConstants.DATE)
                        manager.setFragmentResultListener(
                            PublicConstants.DATE,
                            this@FilteredDocumentsFragment
                        ) { requestKey, bundle ->
                            endDate = bundle.getString(requestKey).toString()
                            dateEnd.text = endDate
                        }
                    }
                }
            }
        }
        userId?.let {

        }
    }
    override fun onChildAdapterClickEventMessage(position: Int, text: String): Snackbar {

        val bar = Snackbar.make(binding.root, text, Snackbar.LENGTH_LONG)
        return bar
    }
    override fun onChildClickStartPutEvent(adapterPosition: Int, itemId: Int, mod: String) {
    }
    override fun onTabClickEvent(position: Int, itemId: Int, text: String) {
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

    override fun onItemInAdapterUsableClickEvent(
        position: Int,
        item: User,
        activatorType: String
    ) {
    }
}