package ru.kondrashen.diplomappv20.presentation.fragments

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.transition.Visibility
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import ru.kondrashen.diplomappv20.R
import ru.kondrashen.diplomappv20.databinding.CreateDocumentEmployeeFragmentBinding
import ru.kondrashen.diplomappv20.databinding.CreateDocumentEmployerFragmentBinding
import ru.kondrashen.diplomappv20.domain.ExtraInfoPageViewModel
import ru.kondrashen.diplomappv20.domain.MainPageViewModel
import ru.kondrashen.diplomappv20.domain.UserAccountControlViewModel
import ru.kondrashen.diplomappv20.presentation.adapters.CustomSpecializationArrayAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.EditDocViewBindingAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleExperienceAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleKnowledgeAdapter
import ru.kondrashen.diplomappv20.presentation.adapters.SimpleSpecializationAdapter
import ru.kondrashen.diplomappv20.presentation.baseClasses.PublicConstants
import ru.kondrashen.diplomappv20.presentation.baseClasses.SearchableBase
import ru.kondrashen.diplomappv20.presentation.baseClasses.onItemClickInterface
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.AddDocumentFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocDependenceFullInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ExperienceInfo
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.Locale

class EditDocumentFragment: Fragment(), onItemClickInterface<Knowledge> {
    private lateinit var userAccountModel: UserAccountControlViewModel
    private lateinit var extraInfoModel: ExtraInfoPageViewModel
    private lateinit var dataModel: MainPageViewModel
    private var knowledgeAdapter: SimpleKnowledgeAdapter? = null
    private var choseKnowledgeAdapter: SimpleKnowledgeAdapter? = null
    private var specializationAdapter: SimpleSpecializationAdapter? = null
    private var experienceAdapter: SimpleExperienceAdapter? = null
    private var experienceNameListAdapter: ArrayAdapter<String>? = null
    private var specSpinnerAdapter: CustomSpecializationArrayAdapter? = null
    private var skillTypeAdapter: ArrayAdapter<String>? = null
    private val formater = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
    private var userId: Int? = null
    private var binding: EditDocViewBindingAdapter? = null
    private var spec: String? = null
    private var exp: String? = null
    private var idsList: MutableList<Int> = mutableListOf()
    private var idsListExp: MutableList<Int> = mutableListOf()
    private var idsListKnow: MutableList<Int> = mutableListOf()
    private var chosedknowledge: MutableList<Int> = mutableListOf()
    private var isSpecOpen: Boolean = false
    private var isKnowOpen: Boolean = false
    private var isExpOpen: Boolean = false
    private var _binding: CreateDocumentEmployeeFragmentBinding? =null
    private var _binding2: CreateDocumentEmployerFragmentBinding? =null
    private val itemTouchHelper by lazy {
        val simpleTouchCallBack = object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val adapter= recyclerView.adapter as SimpleKnowledgeAdapter
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                val knowledges = adapter.getItems()
                Collections.swap(knowledges, from, to)
                choseKnowledgeAdapter?.setItems(knowledges)
                adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (actionState == ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.scaleX = 1.2F
                    viewHolder?.itemView?.scaleY = 1.2F
                    viewHolder?.itemView?.alpha = 0.8F
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.scaleX = 1F
                viewHolder.itemView.scaleY = 1F
                viewHolder.itemView.alpha = 1F
            }

        }
        ItemTouchHelper(simpleTouchCallBack)
    }
    companion object{
        var ADAPTER_IDs = "listOfIndexes"
        var ADAPTER_IDs_EXP = "listOfIndexesEXP"
        var ADAPTER_IDs_KNOW = "listOfIndexesKNOW"
        var IS_SPEC_OPEN = "specState"
        var IS_EXP_OPEN = "expState"
        var IS_KNOW_OPEN = "knowState"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntegerArrayList(ADAPTER_IDs, specializationAdapter?.getCurrentIDs() as ArrayList<Int>)
        outState.putIntegerArrayList(ADAPTER_IDs_EXP, experienceAdapter?.getCurrentIDs() as ArrayList<Int>)
        outState.putIntegerArrayList(ADAPTER_IDs_KNOW, choseKnowledgeAdapter?.getCurrentIDs() as ArrayList<Int>)
        outState.putBoolean(IS_SPEC_OPEN, isSpecOpen)
        outState.putBoolean(IS_KNOW_OPEN, isKnowOpen)
        outState.putBoolean(IS_EXP_OPEN, isExpOpen)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userAccountModel = ViewModelProvider(requireActivity()).get(UserAccountControlViewModel::class.java)
        dataModel = ViewModelProvider(requireActivity()).get(MainPageViewModel::class.java)
        extraInfoModel = ViewModelProvider(requireActivity()).get(ExtraInfoPageViewModel::class.java)
        userAccountModel.getSpecializationInfoFromServ()
        extraInfoModel.getSkillTypesFromServ("allWithSkill")
        extraInfoModel.getStartSkillDataFromServ("all")
        savedInstanceState?.getIntegerArrayList(ADAPTER_IDs)?.let {
            idsList = it.toMutableList()
        }
        savedInstanceState?.getIntegerArrayList(ADAPTER_IDs_EXP)?.let {
            idsListExp = it.toMutableList()
        }
        savedInstanceState?.getIntegerArrayList(ADAPTER_IDs_KNOW)?.let {
            idsListKnow = it.toMutableList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        when(arguments?.getString("docType")){
            "vacancy" -> {
                _binding2 =
                    CreateDocumentEmployerFragmentBinding.inflate(layoutInflater, container, false)
            }
            else -> {
                _binding =
                    CreateDocumentEmployeeFragmentBinding.inflate(layoutInflater, container, false)
            }
        }

        binding = EditDocViewBindingAdapter(_binding, _binding2)
        binding?.let {
            it.availableKnowRecycle?.layoutManager = FlexboxLayoutManager(requireContext())
            it.chosenKnowRecycle?.layoutManager = FlexboxLayoutManager(requireContext())
            itemTouchHelper.attachToRecyclerView(it.chosenKnowRecycle)
            if (savedInstanceState?.getBoolean(IS_SPEC_OPEN) == true){
                isSpecOpen = savedInstanceState.getBoolean(IS_SPEC_OPEN)
                it.specView?.visibility = VISIBLE
                it.specButton?.setBackgroundResource(R.drawable.open_list_with_arrow)
            }
            if (savedInstanceState?.getBoolean(IS_KNOW_OPEN) == true){
                isKnowOpen = savedInstanceState.getBoolean(IS_KNOW_OPEN)
                it.knowView?.visibility = VISIBLE
                it.knowButton?.setBackgroundResource(R.drawable.open_list_with_arrow)
            }
            if (savedInstanceState?.getBoolean(IS_EXP_OPEN) == true){
                isExpOpen = savedInstanceState.getBoolean(IS_EXP_OPEN)
                it.expView?.visibility = VISIBLE
                it.expButton?.setBackgroundResource(R.drawable.open_list_with_arrow)
            }
        }
        userId = arguments?.getInt("userId")
        customizeStartLayout(userId)
        updateUI(userId)
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun customizeStartLayout(userId: Int?){
        // Стартовое отображение списков при повороте экрана
        userId?.let {
            userAccountModel.getUserDocumentDependenceServ(userId)
            if (idsList.isNotEmpty()) {
                userAccountModel.getUserDocumentDependenceByIdsRoom(userId, idsList, "in")
                    .observe(requireActivity()) {
                        val docDependencies = it as MutableList<DocDependenceFullInfo>
                        specializationAdapter?.addDependence(docDependencies)
                        TransitionManager.beginDelayedTransition(binding?.root)
                        if (specializationAdapter?.itemCount != 0)
                            binding?.specRecycler?.visibility = VISIBLE
                        else
                            binding?.specRecycler?.visibility = GONE
                        binding?.specRecycler?.adapter = specializationAdapter
                    }
            }
            if (idsListExp.isNotEmpty()) {
                userAccountModel.getUserExperienceByIdsRoom(userId, idsListExp, "in").observe(requireActivity()) {
                    val experience = it as MutableList<ExperienceInfo>
                    experienceAdapter?.addExperience(experience)
                    TransitionManager.beginDelayedTransition(binding?.root)
                    if(experienceAdapter?.itemCount != 0)
                        binding?.expRecycler?.visibility = VISIBLE
                    else
                        binding?.expRecycler?.visibility = GONE
                    binding?.expRecycler?.adapter = experienceAdapter
                }
            }
            if (idsListKnow.isNotEmpty()){
                extraInfoModel.getKnowledgeByIdsRoom(idsListKnow, "in", null).observe(requireActivity()) { know ->
                    val knowledgeChosen = know as MutableList<Knowledge>
                    choseKnowledgeAdapter = SimpleKnowledgeAdapter(knowledgeChosen, "removableView", this)
                    binding?.chosenKnowRecycle?.adapter = choseKnowledgeAdapter
                }
                extraInfoModel.getKnowledgeByIdsRoom(idsListKnow, "out", null).observe(requireActivity()) { know ->
                    val knowledge = know as MutableList<Knowledge>
                    knowledgeAdapter = SimpleKnowledgeAdapter(knowledge, "simpleView", this)
                    binding?.availableKnowRecycle?.adapter = knowledgeAdapter
                }
            } else{
                extraInfoModel.getKnowledgeByIdsRoom(listOf(), "in", null).observe(requireActivity()) { know ->
                    val knowledgeChosen = know as MutableList<Knowledge>
                    choseKnowledgeAdapter = SimpleKnowledgeAdapter(knowledgeChosen, "removableView", this)
                    binding?.chosenKnowRecycle?.adapter = choseKnowledgeAdapter
                }
                extraInfoModel.getKnowledgeByIdsRoom(listOf(), "out", null).observe(requireActivity()) { know ->
                    val knowledge = know as MutableList<Knowledge>
                    knowledgeAdapter = SimpleKnowledgeAdapter(knowledge, "simpleView", this)
                    binding?.availableKnowRecycle?.adapter = knowledgeAdapter
                }
            }
            // ---------------------------------------------------------------------
        }
    }


    private fun updateUI(userId: Int?) {
        userId?.let { userId ->
            val date = formater.format(Date())

            binding?.apply {
                dateText.text =date
                salaryCheckBox?.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        val transit =TransitionSet()
                        transit.addTransition(ChangeBounds())
                        transit.addTransition(Slide(Gravity.START))
                        transit.addTransition(Fade(Visibility.MODE_IN))
                        transit.addTransition(Fade(Visibility.MODE_OUT))
                        transit.setOrdering(TransitionSet.ORDERING_TOGETHER)
                        transit.duration =300
                        TransitionManager.beginDelayedTransition(binding?.root, transit)
                        salaryMin?.visibility = VISIBLE
                    }
                    else {
                        val transit = TransitionSet()
                        transit.addTransition(Slide(Gravity.END))
                        transit.addTransition(ChangeBounds())
                        transit.setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                        transit.duration = 300
                        TransitionManager.beginDelayedTransition(binding?.root, transit)
                        salaryMin?.visibility = GONE
                    }
                }

                knowFilter?.addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }
                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                    override fun afterTextChanged(s: Editable?) {
                        TransitionManager.beginDelayedTransition(availableKnowRecycle)
                        SearchableBase().filterAdapters(knowFilter.text.toString(), knowledgeAdapter)
                    }

                })
                expSpinner?.let {
                    extraInfoModel.getExperienceTimeNameList().observe(requireActivity()) { names ->
                        val result = names as List<String>
                        experienceNameListAdapter =
                            ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, result)
                        it.adapter = experienceNameListAdapter
                    }
                    expSpinner.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            exp = expSpinner.selectedItem.toString()

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }

                    }
                }
                specSpinner?.let {spiner->
                    extraInfoModel.getSpecializationNames().observe(viewLifecycleOwner){ it ->
                        val adaper = ArrayAdapter(
                            requireActivity(),
                            R.layout.spiner_dropdown_item,
                            it)
                        spiner.adapter= adaper
                    }
                    spiner.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            spec = spiner.selectedItem.toString()

                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }

                    }

                }
                knowTypeSpiner?.let{spiner->
                    extraInfoModel.getSkillTypeNamesFromRoom().observe(viewLifecycleOwner){
                        skillTypeAdapter = ArrayAdapter(requireActivity(), R.layout.spiner_dropdown_item, it)
                        knowTypeSpiner.adapter = skillTypeAdapter
                    }
                    spiner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            var iDs = choseKnowledgeAdapter?.getCurrentIDs() as MutableList<Int>

                            extraInfoModel.getKnowledgeByIdsRoom(iDs, "out", knowTypeSpiner.selectedItem.toString()).removeObservers(viewLifecycleOwner)
                            extraInfoModel.getKnowledgeByIdsRoom(iDs, "out", knowTypeSpiner.selectedItem.toString()).observeOneData(viewLifecycleOwner){ know ->
                                val knowledge = know as MutableList<Knowledge>
                                knowledgeAdapter = SimpleKnowledgeAdapter(knowledge, "simpleView", this@EditDocumentFragment)
                                binding?.availableKnowRecycle?.adapter = knowledgeAdapter
                            }
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }

                }
                // Управление отображением списка
                knowButton?.setOnClickListener { btn ->
                    TransitionManager.beginDelayedTransition(binding?.root)
                    isKnowOpen = !isKnowOpen
                    if (isKnowOpen) {
                        knowView?.visibility = VISIBLE
                        btn.setBackgroundResource(R.drawable.open_list_with_arrow)
                    }
                    else{
                        knowView?.visibility = GONE
                        btn.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    }
                }
                specButton?.setOnClickListener {
                    TransitionManager.beginDelayedTransition(binding?.root)
                    if(specializationAdapter?.itemCount != 0)
                        binding?.specRecycler?.visibility = VISIBLE
                    isSpecOpen = !isSpecOpen
                    if (isSpecOpen) {
                        specView?.visibility = VISIBLE
                        it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    }
                    else {
                        specView?.visibility = GONE
                        it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    }
                }
                expButton?.setOnClickListener {
                    TransitionManager.beginDelayedTransition(binding?.root)
                    isExpOpen = !isExpOpen
                    if (isExpOpen) {
                        expView?.visibility = VISIBLE
                        it.setBackgroundResource(R.drawable.open_list_with_arrow)
                    }
                    else{
                        expView?.visibility = GONE
                        it.setBackgroundResource(R.drawable.hidden_list_with_arrow)
                    }
                }
                saveBtn?.setOnClickListener {
                    binding?.let {
                        val bar = Snackbar.make(binding?.root as ViewGroup, "", Snackbar.LENGTH_LONG)
                        if (it.title?.text.toString() =="") {
                            bar.setText(R.string.not_at_all)
                            bar.show()
                        }
                        else {
                            userAccountModel.postDocument(
                                AddDocumentFullInfo(
                                    it.title?.text.toString(),
                                    it.salaryF?.text.toString(),
                                    it.salaryS?.text.toString(),
                                    it.extraInfo?.text.toString(),
                                    it.contactInfo?.text.toString(),
                                    arguments?.getString("docType") ?: "notIdentify",
                                    userId,
                                    choseKnowledgeAdapter?.getCurrentIDs() ?: listOf(),
                                    experienceAdapter?.getCurrentIDs() ?: listOf(),
                                    specializationAdapter?.getCurrentIDs() ?: listOf()
                                ), userId, mod = "alowed"
                            ).observe(viewLifecycleOwner){status ->
                                if (status == "success") {
                                    bar.setText(R.string.success)
                                    var bundle =  Bundle()
                                    bundle.putInt(PublicConstants.USER_ID, userId)
                                    findNavController().navigate(R.id.action_editDocumentFragment_to_personalSpaceFragment,bundle)
                                }
                                else if (status == "not_at_all"){
                                    bar.setText(R.string.not_at_all_serv)
                                    var bundle =  Bundle()
                                    bundle.putInt(PublicConstants.USER_ID, userId)
                                    findNavController().navigate(R.id.action_editDocumentFragment_to_personalSpaceFragment,bundle)
                                }
                                else if (status == "timeout"){
                                    bar.setText(R.string.server_not_avilable)
                                }
                                else if (status == "403"){
                                    bar.setText(R.string.not_allowed)
                                }
                                else {
                                    bar.setText(R.string.no_connection)
                                }
                                bar.show()
                            }
                        }
                    }
                }
                saveBtnEmr?.setOnClickListener {
                    binding?.let {
                        val bar = Snackbar.make(binding?.root as ViewGroup, "", Snackbar.LENGTH_LONG)
                        if (it.title?.text.toString() =="") {
                            bar.setText(R.string.not_at_all)
                            bar.show()
                        }
                        else {
                            extraInfoModel.getExperienceTimeNameId(exp?: "")
                                .observe(viewLifecycleOwner){expId->
                                    extraInfoModel.getSpecializationId(spec?: "")
                                        .observe(viewLifecycleOwner) { specId ->
                                            userAccountModel.postDocument(
                                                AddDocumentFullInfo(
                                                    it.title?.text.toString(),
                                                    it.salaryF?.text.toString(),
                                                    it.salaryS?.text.toString(),
                                                    it.extraInfo?.text.toString(),
                                                    it.contactInfo?.text.toString(),
                                                    arguments?.getString("docType")
                                                        ?: "notIdentify",
                                                    userId,
                                                    choseKnowledgeAdapter?.getCurrentIDs()
                                                        ?: listOf(),
                                                    listOf(expId),
                                                    listOf(specId)
                                                ), userId, mod = "alowed"
                                            ).observe(viewLifecycleOwner){status ->
                                                if (status == "success") {
                                                    bar.setText(R.string.success)
                                                    var bundle =  Bundle()
                                                    bundle.putInt(PublicConstants.USER_ID, userId)
                                                    findNavController().navigate(R.id.action_editDocumentFragment_to_personalSpaceFragment,bundle)
                                                }
                                                else if (status == "not_at_all"){
                                                    bar.setText(R.string.not_at_all_serv)
                                                    var bundle =  Bundle()
                                                    bundle.putInt(PublicConstants.USER_ID, userId)
                                                    findNavController().navigate(R.id.action_editDocumentFragment_to_personalSpaceFragment,bundle)
                                                }
                                                else if (status == "timeout"){
                                                    bar.setText(R.string.server_not_avilable)
                                                }
                                                else if (status == "403"){
                                                    bar.setText(R.string.not_allowed)
                                                }
                                                else {
                                                    bar.setText(R.string.no_connection)
                                                }
                                                bar.show()
                                            }
                                        }
                            }

                        }
                    }
                }

                cancelBtn?.setOnClickListener {
                    val bundle =  Bundle()
                    bundle.putInt(PublicConstants.USER_ID, userId)
                    findNavController().navigate(R.id.action_editDocumentFragment_to_personalSpaceFragment,bundle)
                }
// ------------------------------------------------
                if (activity != null) {
                    specializationAdapter =
                        SimpleSpecializationAdapter(activity as AppCompatActivity, mutableListOf(), "removableUsage", null, userAccountModel, userId)
                    binding?.specRecycler?.adapter = specializationAdapter
                }
                //Добавление данных по специальности
                specInfoButton?.setOnClickListener {
                    val manager =parentFragmentManager
                    var bundle = Bundle()
                    bundle.putInt("userId", userId)
                    bundle.putString("mod", "chose_spec")
                    bundle.putIntegerArrayList(ADAPTER_IDs, specializationAdapter?.getCurrentIDs() as ArrayList<Int>)
                    val dialog = ChoseItemsDialogFragment()
                    dialog.arguments = bundle
                    dialog.show(manager, "chose_specialization")
                    manager.setFragmentResultListener("result",viewLifecycleOwner){ requestKey, bundle ->
                        val idsList = bundle.getIntegerArrayList("dependenceIds") as MutableList<Int>
                        userAccountModel.getUserDocumentDependenceByIdsRoom(userId, idsList, "in").observe(requireActivity()) {
                            val docDependencies = it as MutableList<DocDependenceFullInfo>
                            specializationAdapter?.addDependence(docDependencies)
                            TransitionManager.beginDelayedTransition(binding?.root)
                            if(specializationAdapter?.itemCount != 0)
                                binding?.specRecycler?.visibility = VISIBLE
                            else
                                binding?.specRecycler?.visibility = GONE
                            binding?.specRecycler?.adapter = specializationAdapter
                        }
                    }
                }
                //Добавление опыта работы
                experienceAdapter = SimpleExperienceAdapter(requireActivity() as AppCompatActivity,mutableListOf(), "removableUsage", null, userAccountModel,null)
                binding?.expRecycler?.adapter = experienceAdapter
                expInfoButton?.setOnClickListener {
                    val manager =parentFragmentManager
                    var bundle = Bundle()
                    bundle.putInt("userId", userId)
                    bundle.putString("mod", "chose_exp")
                    println(experienceAdapter?.getCurrentIDs().toString() +"djn")
                    bundle.putIntegerArrayList(ADAPTER_IDs, experienceAdapter?.getCurrentIDs() as ArrayList<Int>)
                    val dialog = ChoseItemsDialogFragment()
                    dialog.arguments = bundle
                    dialog.show(manager, "chose_experience")
                    manager.setFragmentResultListener("result",viewLifecycleOwner){ requestKey, bundle ->
                        val idsListExp = bundle.getIntegerArrayList("dependenceIds") as MutableList<Int>
                        userAccountModel.getUserExperienceByIdsRoom(userId, idsListExp, "in").observe(requireActivity()) {
                            val experience = it as MutableList<ExperienceInfo>
                            experienceAdapter?.addExperience(experience)
                            TransitionManager.beginDelayedTransition(binding?.root)
                            if(experienceAdapter?.itemCount != 0)
                                binding?.expRecycler?.visibility = VISIBLE
                            else
                                binding?.expRecycler?.visibility = GONE
                            binding?.expRecycler?.adapter = experienceAdapter
                        }
                    }
                }
            }
        }
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

    override fun onChildAdapterClickEventMessage(position: Int, text: String): Snackbar {
        TODO("Not yet implemented")
    }

    override fun onChildClickStartPutEvent(adapterPosition: Int, itemId: Int, mod: String) {
        TODO("Not yet implemented")
    }

    override fun onTabClickEvent(position: Int, itemId: Int, text: String) {
        TODO("Not yet implemented")
    }

    override fun onItemInAdapterUsableClickEvent(
        position: Int,
        item: Knowledge,
        activatorType: String
    ) {
        when(activatorType){
            "removeFromMain" -> choseKnowledgeAdapter?.addItem(item)
            "removeFromChosen" -> knowledgeAdapter?.addItem(item)
        }
    }

}